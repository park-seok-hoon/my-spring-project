package com.minishop.repository.jpa;

import com.minishop.domain.user.User;
import com.minishop.exception.AppException;
import com.minishop.exception.ErrorCode;
import com.minishop.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public User save(User user) {
        em.persist(user);
        return user;
    }

    @Override
    public User update(Long id, User updateUser) {
        User dbUser = em.find(User.class, id);

        if (dbUser == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        dbUser.changeUsername(updateUser.getUsername());
        dbUser.changeEmail(updateUser.getEmail());
        dbUser.changePassword(updateUser.getPassword());

        return dbUser;
    }

    @Override
    public int delete(Long id) {
        User user = em.find(User.class, id);

        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        em.remove(user);

        return 1;
    }

    @Override
    public Optional<User> findById(Long id) {
        User user = em.find(User.class, id);

        return Optional.ofNullable(user);
    }

    @Override
    public List<User> findAll() {
        return em.createQuery(
                "select u from User u",
                User.class
        ).getResultList();
    }

    @Override
    public User findByEmail(String email) {
        List<User> result = em.createQuery(
                        "select u from User u where u.email = :email",
                        User.class
                )
                .setParameter("email", email)
                .getResultList();

        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    public void deleteAll() {
        em.createQuery("delete from User").executeUpdate();
    }
}