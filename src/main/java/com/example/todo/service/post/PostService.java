package com.example.todo.service.post;

import com.example.todo.domain.entity.TeamEntity;
import com.example.todo.domain.entity.image.Image;
import com.example.todo.domain.entity.post.Post;
import com.example.todo.domain.entity.user.User;
import com.example.todo.domain.repository.MemberRepository;
import com.example.todo.domain.repository.TeamReposiotry;
import com.example.todo.domain.repository.image.ImageRepository;
import com.example.todo.domain.repository.post.PostRepository;
import com.example.todo.domain.repository.user.UserRepository;
import com.example.todo.dto.post.request.PostCreateRequestDto;
import com.example.todo.dto.post.request.PostUpdateRequestDto;
import com.example.todo.dto.post.response.PostCreateResponseDto;
import com.example.todo.dto.post.response.PostDeleteResponseDto;
import com.example.todo.dto.post.response.PostListResponseDto;
import com.example.todo.dto.post.response.PostOneResponseDto;
import com.example.todo.exception.TodoAppException;
import com.example.todo.service.image.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.example.todo.exception.ErrorCode.*;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final TeamReposiotry teamReposiotry;
    private final ImageRepository imageRepository;
    private final MemberRepository memberRepository;
    private final ImageService imageService;

    @Transactional
    public PostCreateResponseDto createPost(final PostCreateRequestDto createDto, Long userId, Long teamId) {
        User user = userRepository.getById(userId);
        TeamEntity team = teamReposiotry.getById(teamId);
        validateMember(team, user);
        Post post = postRepository.save(createDto.toEntity(user, team));

        // 이미지 첨부하지 않았을 경우
        if (createDto.getImages() == null) {
            return new PostCreateResponseDto(post);
        }

        // 이미지 첨부했을 경우 - ImageService 만들어서 분리해야 될 거 같음.
        imageService.createImage(createDto, post, teamId);

        return new PostCreateResponseDto(post);
    }

    public Page<PostListResponseDto> readAllPost(Long userId, Long teamId, Integer offset) {
        User user = userRepository.getById(userId);
        TeamEntity team = teamReposiotry.getById(teamId);
        validateMember(team, user);

        Pageable pageable = PageRequest.of(offset, 20, Sort.by("id").descending());
        Page<Post> readAllPost = postRepository.findAllByUserIdAndTeamId(userId, teamId, pageable);
        Page<PostListResponseDto> postListResponseDto = readAllPost.map(post -> new PostListResponseDto(post, user));
//        List<PostListResponseDto> collect = readAllPost.stream().map(p -> new PostListResponseDto(p, user)).collect(Collectors.toList());

        return postListResponseDto;
    }

    public PostOneResponseDto readOnePost(Long userId, Long teamId, Long postId) {
        User user = userRepository.getById(userId);
        TeamEntity team = teamReposiotry.getById(teamId);
        validateMember(team, user);

        Post post = postRepository.getById(postId);
        post.addViewCount();
        return new PostOneResponseDto(post);
    }

    @Transactional
    public PostDeleteResponseDto deletePost(Long userId, Long teamId, Long postId) {
        User user = userRepository.getById(userId);
        TeamEntity team = teamReposiotry.getById(teamId);
        validateMember(team, user);

        Post post = postRepository.getById(postId);
        // 본인이 작성한 글인지 체크
        if (!post.getUser().getId().equals(userId)) {
            throw new TodoAppException(NOT_MATCH_USERID, NOT_MATCH_USERID.getMessage());
        }

        postRepository.delete(post);
        return new PostDeleteResponseDto(post);
    }

    @Transactional
    public PostCreateResponseDto updatePost(final PostUpdateRequestDto updateDto, final Long userId, final Long teamId, final Long postId) {
        User user = userRepository.getById(userId);
        TeamEntity team = teamReposiotry.getById(teamId);
        Post post = postRepository.getById(postId);

        if (!post.getUser().getId().equals(userId)) {
            throw new TodoAppException(NOT_MATCH_USERID, NOT_MATCH_USERID.getMessage());
        }

        post.update(updateDto);
        return new PostCreateResponseDto(post);
    }

    private void validateMember(final TeamEntity team, final User user) {
        memberRepository.findByTeamAndUser(team, user)
                .orElseThrow(() -> new TodoAppException(NOT_FOUND_MEMBER, NOT_FOUND_MEMBER.getMessage()));
    }

}
