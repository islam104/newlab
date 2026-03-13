package com.recruting.demo.repository;

import com.recruting.demo.entity.Vacancy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VacancyRepository extends JpaRepository<Vacancy, Long> {
    
    List<Vacancy> findByStatus(Vacancy.VacancyStatus status);
    
    List<Vacancy> findByHrManager(String hrManager);
    
    List<Vacancy> findByLocationContainingIgnoreCase(String location);
    
    @Query("SELECT v FROM Vacancy v WHERE v.salary >= :minSalary AND v.salary <= :maxSalary")
    List<Vacancy> findBySalaryRange(@Param("minSalary") Double minSalary, @Param("maxSalary") Double maxSalary);
    
    @Query("SELECT v FROM Vacancy v WHERE v.title LIKE %:keyword% OR v.description LIKE %:keyword%")
    List<Vacancy> searchByKeyword(@Param("keyword") String keyword);
    
    Optional<Vacancy> findByTitleAndLocation(String title, String location);
}
