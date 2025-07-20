package com.back.domain.member.address.entity;

import com.back.domain.member.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Address {
    // ------------ [필드] ------------
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT
    @Setter(AccessLevel.PRIVATE)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable=false)
    private String content;

    @Column(nullable=false)
    private Boolean isDefault;

    @ManyToOne(fetch = FetchType.LAZY) //지연 로딩으로 성능 최적화
    @JoinColumn(name= "member_id", referencedColumnName= "id") //외래키 설정
    private Member member;

    // ------------ [생성자] ------------
    public Address(String content, Boolean isDefault, Member member) {
        if (content == null || content.isBlank())
            throw new IllegalArgumentException("주소 내용은 비어있을 수 없습니다.");
        if (member == null)
            throw new IllegalArgumentException("회원 정보는 비어있을 수 없습니다.");

        this.content = content;
        this.isDefault = isDefault;
        this.member = member;
    }

    public Address(String content, Member member){
        this(content, false, member);
    }

    // ------------ [메서드] ------------
    public void setDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public void updateContent(String content) {
        this.content = content;
    }

}
