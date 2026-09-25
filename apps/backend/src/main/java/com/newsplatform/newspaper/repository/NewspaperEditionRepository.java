package com.newsplatform.newspaper.repository;

import com.newsplatform.newspaper.entity.NewspaperEdition;
import com.newsplatform.newspaper.entity.NewspaperStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface NewspaperEditionRepository extends JpaRepository<NewspaperEdition, UUID> {
    List<NewspaperEdition> findAllByOrderByEditionDateDescEditionAsc();
    List<NewspaperEdition> findByStatusOrderByEditionDateDescEditionAsc(NewspaperStatus status);
    boolean existsByEditionDateAndEditionIgnoreCase(LocalDate editionDate, String edition);
    boolean existsByEditionDateAndEditionIgnoreCaseAndIdNot(LocalDate editionDate, String edition, UUID id);
}
