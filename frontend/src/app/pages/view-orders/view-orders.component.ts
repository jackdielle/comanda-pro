import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslocoModule } from '@ngneat/transloco';
import { ApiService, Order } from '../../services/api.service';

@Component({
  selector: 'app-view-orders',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslocoModule],
  templateUrl: './view-orders.component.html',
  styleUrls: ['./view-orders.component.scss']
})
export class ViewOrdersComponent implements OnInit {
  orders: Order[] = [];
  statusFilter = '';
  orderStatuses = ['IN_PREPARAZIONE', 'PRONTO', 'IN_CONSEGNA', 'CONSEGNATO', 'ANNULLATO'];
  loading = true;
  error: string | null = null;
  selectedOrder: Order | null = null;
  newStatus = '';

  constructor(private apiService: ApiService) { }

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading = true;
    this.apiService.getAllOrders().subscribe({
      next: (orders) => {
        this.orders = orders.sort((a, b) => (b.createdAt || 0) - (a.createdAt || 0));
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error loading orders';
        this.loading = false;
      }
    });
  }

  filterOrders(): Order[] {
    if (!this.statusFilter) {
      return this.orders;
    }
    return this.orders.filter(o => o.status === this.statusFilter);
  }

  isOrderFromToday(createdAt?: number): boolean {
    if (!createdAt) return false;
    const orderDate = new Date(createdAt);
    const today = new Date();
    return orderDate.toDateString() === today.toDateString();
  }

  shouldShowCustomer(order: Order): boolean {
    // Show customer info only if data hasn't been removed
    return !order.customerDataRemoved;
  }

  openDetails(order: Order): void {
    this.selectedOrder = order;
    this.newStatus = order.status || '';
  }

  closeDetails(): void {
    this.selectedOrder = null;
  }

  updateStatus(): void {
    if (!this.selectedOrder || !this.newStatus) {
      return;
    }

    this.apiService.updateOrderStatus(this.selectedOrder.id!, this.newStatus).subscribe({
      next: (updatedOrder) => {
        const index = this.orders.findIndex(o => o.id === updatedOrder.id);
        if (index > -1) {
          this.orders[index] = updatedOrder;
        }
        this.selectedOrder = updatedOrder;
      },
      error: (err) => {
        this.error = 'Error updating status';
      }
    });
  }

  deleteOrder(id: number | undefined): void {
    if (!id || !confirm('Are you sure you want to delete this order?')) {
      return;
    }

    this.apiService.deleteOrder(id).subscribe({
      next: () => {
        this.orders = this.orders.filter(o => o.id !== id);
        this.closeDetails();
      },
      error: (err) => {
        this.error = 'Error deleting order';
      }
    });
  }

  getStatusColor(status: string | undefined): string {
    switch (status) {
      case 'IN_PREPARAZIONE':
        return 'bg-blue-100 text-blue-800';
      case 'PRONTO':
        return 'bg-green-100 text-green-800';
      case 'IN_CONSEGNA':
        return 'bg-yellow-100 text-yellow-800';
      case 'CONSEGNATO':
        return 'bg-gray-100 text-gray-800';
      case 'ANNULLATO':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  formatDate(timestamp: number | undefined): string {
    if (!timestamp) return '';
    return new Date(timestamp).toLocaleString('it-IT');
  }
}
