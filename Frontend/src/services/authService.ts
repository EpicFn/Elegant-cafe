import client from "@/src/lib/backend/client";
import type { components } from "@/src/lib/backend/api/schema.d.ts";

type MemberJoinReqBody = components["schemas"]["MemberJoinReqBody"];
type MemberLoginReqBody = components["schemas"]["MemberLoginReqBody"];
type MemberUpdateDto = components["schemas"]["MemberUpdateDto"];
type MemberWithAuthDto = components["schemas"]["MemberWithAuthDto"];
type MemberPasswordVerifyReqBody = components["schemas"]["MemberPasswordVerifyReqBody"];

export interface UserInfo {
  id: number;
  email: string;
  name: string;
  isAdmin: boolean;
}

export class AuthService {
  // 회원가입
  static async signup(data: MemberJoinReqBody): Promise<UserInfo> {
    const { data: response, error } = await client.POST("/api/members/join", {
      body: data,
    });

    if (error) {
      throw new Error("회원가입에 실패했습니다.");
    }

    if (!response?.data) {
      throw new Error("회원가입 응답 데이터가 없습니다.");
    }

    return {
      id: response.data.id,
      email: response.data.email,
      name: response.data.name,
      isAdmin: false, // 회원가입 시에는 일반 사용자
    };
  }

  // 로그인
  static async login(data: MemberLoginReqBody): Promise<UserInfo> {
    try {
      const { data: response, error } = await client.POST("/api/members/login", {
        body: data,
      });

    if (error) {
      throw new Error("로그인에 실패했습니다.");
    }

    if (!response?.data?.member) {
      throw new Error("로그인 응답 데이터가 없습니다.");
    }

      // member 필드가 없을 수 있으므로 확인
      if (!response.data.member) {
        console.error("로그인 응답에 member 정보 없음:", response.data);
        throw new Error("로그인 응답에 사용자 정보가 없습니다.");
      }

      const userInfo = {
        id: response.data.member.id,
        email: response.data.member.email,
        name: response.data.member.name,
        isAdmin: response.data.member.isAdmin,
      };

      console.log("로그인 성공 - 사용자 정보:", userInfo);
      return userInfo;
    } catch (error) {
      console.error("로그인 전체 에러:", error);
      throw error;
    }
  }

  // 사용자 정보 조회
  static async getUserInfo(): Promise<UserInfo> {
    const { data: response, error } = await client.GET("/api/members/info");

    if (error) {
      throw new Error("사용자 정보 조회에 실패했습니다.");
    }

    if (!response?.data) {
      throw new Error("사용자 정보가 없습니다.");
    }

    return {
      id: response.data.id,
      email: response.data.email,
      name: response.data.name,
      isAdmin: response.data.isAdmin,
    };
  }

  // 로그아웃
  static async logout(): Promise<void> {
    const { error } = await client.DELETE("/api/members/logout");

    if (error) {
      throw new Error("로그아웃에 실패했습니다.");
    }
  }

  // 회원 정보 수정
  static async updateMemberInfo(data: MemberUpdateDto): Promise<UserInfo> {
    const { data: response, error } = await client.PUT("/api/members/info", {
      body: data,
    });

    if (error) {
      throw new Error("회원 정보 수정에 실패했습니다.");
    }

    if (!response?.data) {
      throw new Error("회원 정보 수정 응답 데이터가 없습니다.");
    }

    return {
      id: response.data.id,
      email: response.data.email,
      name: response.data.name,
      isAdmin: response.data.isAdmin,
    };
  }

  // 회원 탈퇴
  static async withdraw(): Promise<void> {
    const { error } = await client.DELETE("/api/members/withdraw");

    if (error) {
      throw new Error("회원 탈퇴에 실패했습니다.");
    }
  }

  // 비밀번호 검증
  static async verifyPassword(password: string): Promise<boolean> {
    const { data: response, error } = await client.POST("/api/members/verify-password", {
      body: { password },
    });

    if (error) {
      // 비밀번호가 틀린 경우
      return false;
    }

    // 성공적으로 검증된 경우
    return true;
  }
} 