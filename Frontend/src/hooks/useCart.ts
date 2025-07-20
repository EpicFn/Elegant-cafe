import { useCart as useCartContext } from '@/src/store/cart';

export const useCart = () => {
  const cartContext = useCartContext();
  
  return {
    ...cartContext,
    totalItems: cartContext.cartItems.reduce((sum, item) => sum + item.quantity, 0),
    totalPrice: cartContext.cartItems.reduce((sum, item) => sum + (item.product.price * item.quantity), 0),
  };
}; 