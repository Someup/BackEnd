package project.backend.business.post.implement;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import project.backend.business.common.DateTimeManager;
import project.backend.business.post.dto.PostDetailDto;
import project.backend.business.post.dto.PostListDto;
import project.backend.common.error.CustomException;
import project.backend.common.error.ErrorCode;
import project.backend.dao.post.entity.Post;
import project.backend.dao.post.repository.PostRepository;
import project.backend.dao.tag.respository.TagRepository;
import project.backend.dao.user.entity.User;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class PostReader {
    private final PostRepository postRepository;
    private final TagRepository tagRepository;

    public List<PostListDto> readPostsWithTags(User user) {
        List<Post> postList = postRepository.findAllByUserId(user.getId());
        List<Long> postIdList = postList.stream().map(
                Post::getId
        ).toList();

        List<Object[]> tagResults = tagRepository.findPostIdAndTagNamesByPostIds(postIdList);
        Map<Long, List<String>> postTagMap = tagResults.stream().collect(
                Collectors.groupingBy(
                        res -> (Long) res[0],
                        Collectors.mapping(res -> (String) res[1], Collectors.toList())
                )
        );

        return postList.stream().map(
                p -> PostListDto.builder()
                        .id(p.getId())
                        .title(p.getTitle())
                        .createdAt(DateTimeManager.convertToStringPattern1(p.getCreatedAt()))
                        .tagList(postTagMap.get(p.getId()))
                        .build()
        ).collect(Collectors.toList());
    }

    public PostDetailDto readPostDetailWithTags(User user, Long postId) {
        Post postDetail = postRepository.findPostByIdAndUser(postId, user);

        if (postDetail == null) {
            log.info("[ERROR] readPostDetailWithTags userId: {}, postId: {}", user.getId(), postId);
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        List<String> tagList = tagRepository.findTagNamesByPostId(postId);

        return PostDetailDto.builder()
                .title(postDetail.getTitle())
                .content(postDetail.getContent())
                .url(postDetail.getUrl())
                .tagList(tagList)
                .createdAt(DateTimeManager.convertToStringPattern2(postDetail.getCreatedAt()))
                .memoContent(postDetail.getMemo())
                .memoCreatedAt(DateTimeManager.convertToStringPattern3(postDetail.getMemoCreatedAt()))
                .build();
    }
}
