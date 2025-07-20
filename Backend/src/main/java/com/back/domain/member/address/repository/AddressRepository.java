package com.back.domain.member.address.repository;

import com.back.domain.member.address.entity.Address;
import com.back.domain.member.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findAllByMember(Member member);
    Optional<Address> findByMemberAndContent(Member member, String content);
}
