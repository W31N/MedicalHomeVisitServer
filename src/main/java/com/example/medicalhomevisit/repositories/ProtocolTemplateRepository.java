package com.example.medicalhomevisit.repositories;

import com.example.medicalhomevisit.models.entities.ProtocolTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProtocolTemplateRepository extends JpaRepository<ProtocolTemplate, UUID> {

    /**
     * Найти шаблон по имени
     */
    Optional<ProtocolTemplate> findByName(String name);

    /**
     * Найти все активные шаблоны (упорядоченные по имени)
     */
    @Query("SELECT pt FROM ProtocolTemplate pt ORDER BY pt.name ASC")
    List<ProtocolTemplate> findAllOrderByName();

    /**
     * Найти шаблоны по категории (если будет добавлено поле category)
     */
    // List<ProtocolTemplate> findByCategoryOrderByName(String category);

    /**
     * Поиск шаблонов по части имени или описания
     */
    @Query("SELECT pt FROM ProtocolTemplate pt WHERE " +
            "LOWER(pt.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(pt.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "ORDER BY pt.name ASC")
    List<ProtocolTemplate> findByNameOrDescriptionContainingIgnoreCase(@Param("searchTerm") String searchTerm);

    /**
     * Проверить существование шаблона с таким именем (исключая текущий по ID)
     */
    @Query("SELECT CASE WHEN COUNT(pt) > 0 THEN true ELSE false END FROM ProtocolTemplate pt " +
            "WHERE LOWER(pt.name) = LOWER(:name) AND pt.id != :excludeId")
    boolean existsByNameIgnoreCaseAndIdNot(@Param("name") String name, @Param("excludeId") UUID excludeId);

    /**
     * Проверить существование шаблона с таким именем
     */
    boolean existsByNameIgnoreCase(String name);
}