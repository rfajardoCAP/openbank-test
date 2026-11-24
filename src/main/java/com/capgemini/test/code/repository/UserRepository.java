package com.capgemini.test.code.repository;

import com.capgemini.test.code.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  boolean existsByEmail(String email);

  Optional<User> findByIdAndRoomId(Long id, Long roomId);
}
