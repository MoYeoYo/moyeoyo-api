package com.example.todo.api.post;

import com.example.todo.domain.Response;
import com.example.todo.dto.post.request.PostCreateRequestDto;
import com.example.todo.dto.post.request.PostUpdateRequestDto;
import com.example.todo.dto.post.response.PostCreateResponseDto;
import com.example.todo.dto.post.response.PostDeleteResponseDto;
import com.example.todo.dto.post.response.PostListResponseDto;
import com.example.todo.dto.post.response.PostOneResponseDto;
import com.example.todo.service.post.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class PostApiController {

    private final PostService postService;

    @PostMapping("/{teamId}/post")
    public Response<PostCreateResponseDto> createPost(final PostCreateRequestDto createDto,
                                                      final Authentication authentication,
                                                      @PathVariable final Long teamId) {
        Long userId = Long.parseLong(authentication.getName());
        return Response.success(postService.createPost(createDto, userId, teamId));
    }

    @GetMapping("/{teamId}/post")
    public Response<Page<PostListResponseDto>> readAllPost(final Authentication authentication,
                                                          @PathVariable final Long teamId,
                                                          @RequestParam(value = "offset", defaultValue = "0") Integer offset) {
         Long userId = Long.parseLong(authentication.getName());
         return Response.success(postService.readAllPost(userId, teamId, offset));
    }

    @GetMapping("/{teamId}/post/{postId}")
    public Response<PostOneResponseDto> readOnePost(final Authentication authentication,
                                                    @PathVariable final Long teamId,
                                                    @PathVariable final Long postId) {
        Long userId = Long.parseLong(authentication.getName());
        return Response.success(postService.readOnePost(userId, teamId, postId));
    }

    @DeleteMapping("/{teamId}/post/{postId}")
    public Response<PostDeleteResponseDto> deletePost(final Authentication authentication,
                                                      @PathVariable final Long teamId,
                                                      @PathVariable final Long postId) {
        Long userId = Long.parseLong(authentication.getName());
        return Response.success(postService.deletePost(userId, teamId, postId));
    }

    @PutMapping("/{teamId}/post/{postId}")
    public Response<PostCreateResponseDto> updatePost(@RequestBody final PostUpdateRequestDto updateDto,
                                                      final Authentication authentication,
                                                      @PathVariable final Long teamId,
                                                      @PathVariable final Long postId) {
        Long userId = Long.parseLong(authentication.getName());
        return Response.success(postService.updatePost(updateDto, userId, teamId, postId));
    }
}
