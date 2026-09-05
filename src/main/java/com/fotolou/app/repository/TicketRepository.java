package com.fotolou.app.repository;

import com.fotolou.app.domain.Ticket;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Ticket entity.
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {
    @Query("select ticket from Ticket ticket where ticket.user.login = ?#{authentication.name} order by ticket.createdDate desc")
    List<Ticket> findByUserIsCurrentUser();

    List<Ticket> findBySalonIdAndCategoryOrderByTicketNumberAsc(Long salonId, com.fotolou.app.domain.enumeration.TicketCategory category);

    List<Ticket> findBySalonIdOrderByCreatedDateDesc(Long salonId);

    List<Ticket> findBySalonIdOrderByTicketNumberAsc(Long salonId);

    List<Ticket> findBySalonIdAndStatusInOrderByTicketNumberAsc(
        Long salonId,
        List<com.fotolou.app.domain.enumeration.TicketStatus> statuses
    );

    List<Ticket> findByUserIdOrderByCreatedDateDesc(Long userId);

    @Query("select max(t.ticketNumber) from Ticket t where t.salon.id = :salonId and t.createdDate >= :startOfDay")
    Integer findMaxTicketNumberForSalonAndDay(@Param("salonId") Long salonId, @Param("startOfDay") java.time.Instant startOfDay);

    long countBySalonIdAndStatusIn(Long salonId, List<com.fotolou.app.domain.enumeration.TicketStatus> statuses);

    default Optional<Ticket> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Ticket> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Ticket> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select ticket from Ticket ticket left join fetch ticket.salon left join fetch ticket.user",
        countQuery = "select count(ticket) from Ticket ticket"
    )
    Page<Ticket> findAllWithToOneRelationships(Pageable pageable);

    @Query("select ticket from Ticket ticket left join fetch ticket.salon left join fetch ticket.user")
    List<Ticket> findAllWithToOneRelationships();

    @Query("select ticket from Ticket ticket left join fetch ticket.salon left join fetch ticket.user where ticket.id =:id")
    Optional<Ticket> findOneWithToOneRelationships(@Param("id") Long id);
}
