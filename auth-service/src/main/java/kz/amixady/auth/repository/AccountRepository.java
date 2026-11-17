package kz.amixady.auth.repository;

import kz.amixady.auth.entity.Account;
import kz.amixady.auth.entity.EmailAddress;
import kz.amixady.auth.exception.AccountNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    boolean existsByEmail(EmailAddress email);

    Optional<Account> findByEmail(EmailAddress email);

    default Account getAccount(UUID id){
        return this.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
    }

}
