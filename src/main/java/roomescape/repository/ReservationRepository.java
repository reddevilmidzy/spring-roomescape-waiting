package roomescape.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.repository.dto.ReservationRankResponse;
import roomescape.service.exception.ReservationNotFoundException;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @EntityGraph(attributePaths = {"member", "theme", "time"})
    List<Reservation> findAll();

    List<Reservation> findAllByDateAndThemeId(LocalDate date, long themeId);

    @EntityGraph(attributePaths = {"member", "theme", "time"})
    List<Reservation> findAllByDateIsGreaterThanEqual(LocalDate date);

    default Reservation fetchById(long id) {
        return findById(id).orElseThrow(() -> new ReservationNotFoundException("존재하지 않는 예약입니다."));
    }

    boolean existsByTimeId(long timeId);

    boolean existsByThemeId(long themeId);

    boolean existsByIdBeforeAndThemeIdAndTimeIdAndDate(long id, long themeId, long timeId, LocalDate date);

    boolean existsByMemberAndThemeAndTimeAndDate(Member member, Theme theme, ReservationTime time, LocalDate date);

    boolean existsByMemberIdAndThemeIdAndTimeIdAndDate(long memberId, long themeId, long timeId, LocalDate date);

    @Query("""
            SELECT r.theme FROM Reservation r
            WHERE r.date BETWEEN :from AND :until
            GROUP BY r.theme
            ORDER BY COUNT(r.theme) DESC
            LIMIT :limitCount
            """)
    List<Theme> findPopularThemes(@Param("from") LocalDate from,
                                  @Param("until") LocalDate until,
                                  @Param("limitCount") int limitCount);

    @Query("""
            SELECT r FROM Reservation r
            JOIN FETCH r.theme
            JOIN FETCH r.time
            JOIN FETCH r.member
            WHERE (:dateFrom IS NULL OR r.date >= :dateFrom)
                AND (:dateTo IS NULL OR r.date <= :dateTo)
                AND (:themeId IS NULL OR r.theme.id = :themeId)
                AND (:memberId IS NULL OR r.member.id = :memberId)
            """)
    List<Reservation> findReservationsByCondition(@Nullable @Param("dateFrom") LocalDate dateFrom,
                                                  @Nullable @Param("dateTo") LocalDate dateTo,
                                                  @Nullable @Param("themeId") Long themeId,
                                                  @Nullable @Param("memberId") Long memberId);

    @Query("""
            SELECT new roomescape.repository.dto.ReservationRankResponse
            (r.id, r.theme.name, r.date, r.time.startAt,
                (SELECT COUNT(r2) AS waiting_rank FROM Reservation r2
                WHERE r.id >= r2.id AND r.time = r2.time AND r.date = r2.date AND r.theme = r2.theme
                )
            )
            FROM Reservation r
            WHERE r.member.id = :memberId
            """)
    List<ReservationRankResponse> findMyReservation(@Param("memberId") long memberId);

    @Query("""
            SELECT r FROM Reservation r JOIN FETCH r.member JOIN FETCH r.theme JOIN FETCH r.time
            WHERE (r.theme, r.time, r.date) IN
            (SELECT r2.theme, r2.time, r2.date FROM Reservation r2 GROUP BY r2.theme, r2.time, r2.date)
            AND r.id NOT IN (SELECT MIN(r3.id) FROM Reservation r3 GROUP BY r3.theme, r3.time, r3.date)
            """)
    List<Reservation> findAllWaiting();
}
