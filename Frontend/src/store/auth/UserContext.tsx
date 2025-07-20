"use client";

import {
  createContext,
  useContext,
  useState,
  ReactNode,
  useCallback,
  useEffect,
} from "react";
import { AuthService } from "@/src/services";

// UserContext: 인증/회원정보 관련 전역 상태 및 함수 제공

export interface UserInfo {
  id: number;
  email: string;
  name: string;
  isAdmin: boolean;
}

// Context에서 제공하는 값/함수 타입
interface UserContextType {
  user: UserInfo | null; // 현재 로그인한 사용자 정보
  isAuthenticated: boolean; // 로그인 여부
  isInitialized: boolean; // 초기화 완료 여부
  setUser: (user: UserInfo) => void; // 직접 사용자 정보 설정(내부용)
  clearUser: () => void; // 사용자 정보/인증 상태 초기화(로그아웃)
  fetchUserInfo: () => Promise<void>; // 사용자 정보 조회회
  login: (email: string, password: string) => Promise<void>; // 로그인
  signup: (name: string, email: string, password: string) => Promise<void>; // 회원가입
  updateUserInfo: (data: { name?: string; password?: string }) => Promise<void>; // 회원정보 수정
  withdraw: () => Promise<void>; // 회원 탈퇴
}

const UserContext = createContext<UserContextType | undefined>(undefined);

// Context 사용 훅
export const useUser = () => {
  const context = useContext(UserContext);
  if (!context) {
    throw new Error("useUser must be used within a UserProvider");
  }
  return context;
};

// Provider 구현
export const UserProvider = ({ children }: { children: ReactNode }) => {
  // 현재 로그인한 사용자 정보 (null이면 미로그인)
  const [user, setUserState] = useState<UserInfo | null>(null);
  const [isInitialized, setIsInitialized] = useState(false);

  // 초기화 시 사용자 정보 조회 (쿠키에 토큰이 있으면 자동 로그인)
  useEffect(() => {
    const initializeUser = async () => {
      try {
        const userInfo = await AuthService.getUserInfo();
        setUserState(userInfo);
      } catch (error) {
        // 토큰이 없거나 만료된 경우 로그인되지 않은 상태로 처리
        console.log("사용자 정보 조회 실패 (로그인되지 않음):", error);
        setUserState(null);
      } finally {
        setIsInitialized(true);
      }
    };

    initializeUser();
  }, []);

  // 내부용: 사용자 정보 직접 설정
  const setUser = (user: UserInfo) => {
    setUserState(user);
  };

  // 로그아웃: 사용자 정보 초기화
  const clearUser = useCallback(async () => {
    // 로컬 상태를 먼저 초기화 (즉시 UI 반영)
    setUserState(null);

    try {
      await AuthService.logout();
    } catch (error) {
      console.error("로그아웃 오류:", error);
      // 서버 로그아웃 실패해도 로컬 상태는 이미 초기화됨
    }
  }, []);

  // 사용자 정보 조회
  const fetchUserInfo = useCallback(async () => {
    try {
      const userInfo = await AuthService.getUserInfo();
      setUserState(userInfo);
    } catch (error) {
      console.error("사용자 정보 조회 오류:", error);
      throw error;
    }
  }, []);

  // 로그인
  const login = useCallback(async (email: string, password: string) => {
    try {
      const userInfo = await AuthService.login({ email, password });
      setUserState(userInfo);
    } catch (error) {
      console.error("로그인 오류:", error);
      throw error;
    }
  }, []);

  // 회원가입
  const signup = useCallback(
    async (name: string, email: string, password: string) => {
      try {
        await AuthService.signup({ email, password, name });
      } catch (error) {
        console.error("회원가입 오류:", error);
        throw error;
      }
    },
    []
  );

  // 회원정보 수정
  const updateUserInfo = useCallback(
    async (data: { name?: string; password?: string }) => {
      try {
        // 현재 사용자 정보 가져오기
        if (!user) {
          throw new Error("로그인이 필요합니다.");
        }

        // 수정할 데이터 준비 (email은 필수)
        const updateData = {
          email: user.email,
          name: data.name || user.name,
          password: data.password || "", // 비밀번호가 제공되지 않으면 빈 문자열
        };

        const updatedUserInfo = await AuthService.updateMemberInfo(updateData);
        setUserState(updatedUserInfo);
      } catch (error) {
        console.error("회원 정보 수정 오류:", error);
        throw error;
      }
    },
    [user]
  );

  // 회원 탈퇴
  const withdraw = useCallback(async () => {
    try {
      await AuthService.withdraw();
      // setUserState(null) 제거 - SettingsPanel에서 직접 처리
    } catch (error) {
      console.error("회원 탈퇴 오류:", error);
      throw error;
    }
  }, []);

  return (
    <UserContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        isInitialized,
        setUser,
        clearUser,
        fetchUserInfo,
        login,
        signup,
        updateUserInfo,
        withdraw,
      }}
    >
      {children}
    </UserContext.Provider>
  );
};
