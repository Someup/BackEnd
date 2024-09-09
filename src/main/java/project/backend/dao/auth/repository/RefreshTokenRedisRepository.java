package project.backend.dao.auth.repository;

import org.springframework.data.repository.CrudRepository;
import project.backend.common.auth.token.RefreshToken;

public interface RefreshTokenRedisRepository extends CrudRepository<RefreshToken, Long> {

  RefreshToken findByRefreshToken(String refreshToken);
}