package com.fotolou.app.repository;

import com.fotolou.app.domain.Ticket;
import jakarta.persistence.LockModeType;
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

    List<Ticket> findBySalonIdAndCategoryOrderByCreatedDateAscIdAsc(
        Long salonId,
        com.fotolou.app.domain.enumeration.TicketCategory category
    );

    List<Ticket> findBySalonIdOrderByCreatedDateDesc(Long salonId);

    List<Ticket> findBySalonIdAndStatusInOrderByCreatedDateAscIdAsc(
        Long salonId,
        List<com.fotolou.app.domain.enumeration.TicketStatus> statuses
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        "select ticket from Ticket ticket where ticket.salon.id = :salonId and ticket.status in :statuses order by ticket.createdDate asc, ticket.id asc"
    )
    List<Ticket> findBySalonIdAndStatusInQueueOrderForUpdate(
        @Param("salonId") Long salonId,
        @Param("statuses") List<com.fotolou.app.domain.enumeration.TicketStatus> statuses
    );

    List<Ticket> findByUserIdOrderByCreatedDateDesc(Long userId);

    @Query(
        "select max(t.ticketNumber) from Ticket t where t.salon.id = :salonId and t.createdDate >= :startOfDay and t.createdDate < :startOfNextDay"
    )
    Integer findMaxTicketNumberForSalonAndDay(
        @Param("salonId") Long salonId,
        @Param("startOfDay") java.time.Instant startOfDay,
        @Param("startOfNextDay") java.time.Instant startOfNextDay
    );

    @Query("select ticket.salon.id from Ticket ticket where ticket.id = :ticketId")
    Optional<Long> findSalonIdByTicketId(@Param("ticketId") Long ticketId);

    long countBySalonIdAndStatusIn(Long salonId, List<com.fotolou.app.domain.enumeration.TicketStatus> statuses);

    @Query("select count(t) from Ticket t where t.status in :statuses")
    long countByStatusIn(@Param("statuses") List<com.fotolou.app.domain.enumeration.TicketStatus> statuses);

    @Query("select t.user.id, count(t) from Ticket t where t.user.id in :userIds group by t.user.id")
    List<Object[]> countTicketsByUserIds(@Param("userIds") java.util.Collection<Long> userIds);

    @Query("select t.ownerPhone, count(t) from Ticket t where t.user is null and t.ownerPhone in :phones group by t.ownerPhone")
    List<Object[]> countTicketsByOwnerPhonesWithoutUser(@Param("phones") java.util.Collection<String> phones);

    @Query("select count(t) from Ticket t where t.user.id = :userId or (:phone is not null and t.ownerPhone = :phone)")
    long countByUserIdOrPhone(@Param("userId") Long userId, @Param("phone") String phone);

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
