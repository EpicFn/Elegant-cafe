export interface UserInfo {
  id: number;
  email: string;
  name: string;
  phoneNumber: string;
  address: string;
  role: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignupRequest {
  email: string;
  password: string;
  name: string;
  phoneNumber: string;
  address: string;
} 