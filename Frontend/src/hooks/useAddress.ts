import { useAddressContext } from '@/src/store/address';

export const useAddress = () => {
  const addressContext = useAddressContext();
  
  return {
    ...addressContext,
    defaultAddress: addressContext.addresses.find(addr => addr.isDefault),
  };
}; 