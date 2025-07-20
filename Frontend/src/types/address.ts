export interface Address {
  id: number;
  address: string;
  detailAddress: string;
  zipCode: string;
  isDefault: boolean;
}

export interface AddressSubmitResponse {
  success: boolean;
  message: string;
  address?: Address;
}

export interface AddressUpdateResponse {
  success: boolean;
  message: string;
} 