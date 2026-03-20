export enum OrderStatus {
  IN_PREPARATION = 'IN_PREPARATION',
  READY = 'READY',
  IN_DELIVERY = 'IN_DELIVERY',
  DELIVERED = 'DELIVERED',
  CANCELLED = 'CANCELLED'
}

export const ORDER_STATUS_LABELS: { [key in OrderStatus]: string } = {
  [OrderStatus.IN_PREPARATION]: 'orders.IN_PREPARATION',
  [OrderStatus.READY]: 'orders.READY',
  [OrderStatus.IN_DELIVERY]: 'orders.IN_DELIVERY',
  [OrderStatus.DELIVERED]: 'orders.DELIVERED',
  [OrderStatus.CANCELLED]: 'orders.CANCELLED'
};

export function getOrderStatusLabel(status?: string): string {
  return ORDER_STATUS_LABELS[status as OrderStatus] || '';
}

export const ORDER_STATUSES = Object.values(OrderStatus);
