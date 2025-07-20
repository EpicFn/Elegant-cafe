import { useOrders } from '@/src/store/order';

export const useOrder = () => {
  const orderContext = useOrders();
  
  return {
    ...orderContext,
    pendingOrders: orderContext.orders.filter(order => order.state === 'ORDERED'),
    completedOrders: orderContext.orders.filter(order => order.state === 'COMPLETED'),
  };
}; 