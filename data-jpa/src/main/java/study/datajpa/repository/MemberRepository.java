package study.datajpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    // 실무에서 메서드 이름으로 쿼리를 생성하는 기능은 간단한 메서드 생성할 때 사용
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    // JPA NamedQuery, Entity에 정의되어 있음
    // 오류 시 애플리케이션 로딩시 에러
    // 어노테이션 생략해도 동작
    // 밑에 기능이랑 같지만 쿼리를 Entity에 작성해야 한다. - 실무에서 잘 안 쓴다.
    @Query(name = "Member.findByUsername")
    List<Member> findByUsername(@Param("username") String username);

    // 오류 시 애플리케이션 로딩시 에러
    // NamedQuery 와 똑같이 동작 - 실무에서 자주 쓴다.
    @Query("select m from Member m where m.username= :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);

    // Dto 생성자 필요
    @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDTO();

    // 파라미터 바인딩
    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") Collection<String> names);

    // 반환 타입 - collection -> 없어도 null이 아닌 empty collection 반환
    List<Member> findListByUsername(String username);

    // 반환 타입 - 단건 -> 없으면 null 반환
    Member findMemberByUsername(String username);

    // 반환 타입 - Optional<>
    Optional<Member> findOptionalByUsername(String username);

//    Page<Member> findByAge(int age, Pageable pageable);

    // 카운터 쿼리가 조인으로 복잡해지면 성능이 안나온다.
    // 카운터 쿼리는 조인할 필요가 없으므로
    // @Query countQuery 으로 카운터 쿼리 분리
    // @Query(value = "select m from Member m left join m.team t",
    //       countQuery = "select count(m) from Member m")
    @Query(countQuery = "select count(m) from Member m")
    Page<Member> findByAge(int age, Pageable pageable);  // 페이징 -> 테스트 코드에서 PageRequest.of(0, 3) 으로 테스트

    // 벌크 쿼리리
    @Modifying(clearAutomatically = true) // 필수. 이게 없으면 DB에는 41살로 업데이트 되는데, 영속성컨텍스트에는 40살로 남아있다.
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);


    // fetch join
    @Override
    @EntityGraph(attributePaths = {"team"}) // fetch join으로 지연로딩 없이 한번에 가져온다.
    List<Member> findAll();

    @EntityGraph(attributePaths = {"team"})
    @Query("select m from Member m")
    List<Member> findMemberEntityGraph(); // JPQL로 쿼리를 직접 작성할 때도 EntityGraph 사용 가능

    @EntityGraph(attributePaths = {"team"})
    List<Member> findEntityGraphByUsername(@Param("username") String username);


    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Member findReadOnlyByUsername(String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Member> findLockByUsername(String name);
}
