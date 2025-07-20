package com.back.domain.member.member.entity;

import com.back.domain.member.address.entity.Address;
import com.back.domain.order.entity.Order;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;

@Entity
@Getter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Member {
    // ------------ [필드] ------------

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT
    @Setter(AccessLevel.PRIVATE)
    @EqualsAndHashCode.Include
    private Long id;

    @Email
    @Column(length = 150, nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50, unique = true)
    private String name; // 가변 닉네임

    @Column(nullable = false, unique = true)
    private String apiKey;

    @Column(nullable = false)
    private boolean isAdmin;

    @OneToMany(
        mappedBy = "member",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @OrderBy("id ASC") // 주소는 등록된 순서대로 정렬
    private final List<Address> addresses = new ArrayList<>();

    @OneToMany(
        mappedBy = "customer",
        fetch = FetchType.LAZY,
        cascade = {CascadeType.PERSIST, CascadeType.REMOVE},
        orphanRemoval = true
    )
    @OrderBy("createdDate DESC") // 주문은 최신순으로 정렬
    private final List<Order> orders = new ArrayList<>();

    // ------------ [생성자 + 빌더] ------------

    @Builder
    public Member(Long id, String email, String password, String name, Boolean isAdmin, String apiKey) {
        if (email == null || email.trim().isEmpty())
            throw new IllegalArgumentException("이메일은 비어있을 수 없습니다.");
        if (password == null || password.trim().isEmpty())
            throw new IllegalArgumentException("비밀번호는 비어있을 수 없습니다.");
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("이름은 비어있을 수 없습니다.");

        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.apiKey = (apiKey != null) ? apiKey : UUID.randomUUID().toString();
        this.isAdmin = (isAdmin != null) ? isAdmin : false;
    }

    // ------------ [메서드] ------------
    public boolean isAdmin() {
        return isAdmin;
    }

    public void updateName(String newName) {
        if (newName == null || newName.trim().isEmpty())
            throw new IllegalArgumentException("이름은 비어있을 수 없습니다.");
        this.name = newName;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getAuthoritiesAsStringList()
                .stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    private List<String> getAuthoritiesAsStringList() {
        List<String> authorities = new ArrayList<>();

        if (isAdmin())
            authorities.add("ROLE_ADMIN");

        return authorities;
    }

    public void modifyApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Optional<Address> getLastAddress() {
        if (addresses.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(addresses.get(addresses.size() - 1));
    }

    public Optional<Address> getDefaultAddress() {
        return addresses.stream()
                .filter(Address::getIsDefault)
                .findFirst();
    }

    public void modifyInfo(String email, String password, String name) {
        if (name != null && !name.isBlank())
            this.name = name;

        if (email != null && !email.isBlank())
            this.email = email;

        if (password != null && !password.isBlank())
            this.password = password;
    }
}
