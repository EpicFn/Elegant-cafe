// 비밀번호 검증 유틸리티 함수들

/**
 * 비밀번호 유효성 검사
 * - 8자리 이상
 */
export const validatePassword = (password: string): { isValid: boolean; message: string } => {
  if (!password) {
    return { isValid: false, message: "비밀번호를 입력해 주세요." };
  }

  if (password.length < 8) {
    return { isValid: false, message: "비밀번호는 8자리 이상이어야 합니다." };
  }

  return { isValid: true, message: "유효한 비밀번호입니다." };
};

/**
 * 비밀번호 확인 검사
 */
export const validatePasswordConfirm = (password: string, confirmPassword: string): { isValid: boolean; message: string } => {
  if (!confirmPassword) {
    return { isValid: false, message: "비밀번호 확인을 입력해 주세요." };
  }

  if (password !== confirmPassword) {
    return { isValid: false, message: "비밀번호가 일치하지 않습니다." };
  }

  return { isValid: true, message: "비밀번호가 일치합니다." };
};

/**
 * 비밀번호 강도 검사 (추가 기능)
 */
export const getPasswordStrength = (password: string): 'weak' | 'medium' | 'strong' => {
  if (!password) return 'weak';

  let score = 0;
  
  // 길이 점수
  if (password.length >= 8) score += 1;
  if (password.length >= 12) score += 1;
  
  // 문자 종류 점수
  if (/[a-z]/.test(password)) score += 1; // 소문자
  if (/[A-Z]/.test(password)) score += 1; // 대문자
  if (/[0-9]/.test(password)) score += 1; // 숫자
  if (/[^a-zA-Z0-9]/.test(password)) score += 1; // 특수문자

  if (score <= 2) return 'weak';
  if (score <= 4) return 'medium';
  return 'strong';
}; 