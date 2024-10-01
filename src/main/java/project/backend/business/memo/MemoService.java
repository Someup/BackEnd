package project.backend.business.memo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.backend.business.memo.implement.MemoManager;
import project.backend.business.memo.request.CreateUpdateMemoServiceRequest;
import project.backend.business.post.implement.PostReader;
import project.backend.entity.post.Post;

@Service
@RequiredArgsConstructor
public class MemoService {

  private final PostReader postReader;
  private final MemoManager memoManager;

  @Transactional
  public void createMemo(Long userId, CreateUpdateMemoServiceRequest memoServiceRequest) {
    Post post = postReader.readActivatedPublishedPost(userId, memoServiceRequest.getPostId());
    memoManager.addMemo(post, memoServiceRequest.getContent());
  }

}