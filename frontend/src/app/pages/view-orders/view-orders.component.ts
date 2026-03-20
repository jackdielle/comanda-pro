import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslocoModule } from '@ngneat/transloco';
import { ApiService, Order, Product, OrderRow } from '../../services/api.service';
import { OrderStatus, ORDER_STATUSES, getOrderStatusLabel } from '../../enums/order-status.enum';
import { ProductCategory, PRODUCT_CATEGORIES, getProductCategoryLabel } from '../../enums/product-category.enum';

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
  orderStatuses = ORDER_STATUSES;
  loading = true;
  error: string | null = null;
  selectedOrder: Order | null = null;
  newStatus = '';
  isEditMode = false;
  editDeliveryTime = '';
  editNotes = '';
  editPaymentMethod = '';
  isSaving = false;
  editLines: OrderRow[] = [];
  editCategories = PRODUCT_CATEGORIES;
  editSelectedCategory = '';
  editProductsByCategory: Product[] = [];
  editSelectedProduct: Product | null = null;
  editQuantity = 1;
  editIsPinsa = false;
  editNoLactose = false;
  editRowNotes = '';
  getOrderStatusLabel = getOrderStatusLabel;
  getProductCategoryLabel = getProductCategoryLabel;

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
    // Show customer info only if customer data exists and hasn't been removed
    return order.customer != null && !order.customerDataRemoved;
  }

  openDetails(order: Order): void {
    this.selectedOrder = order;
    this.newStatus = order.status || '';
  }

  closeDetails(): void {
    this.selectedOrder = null;
    this.isEditMode = false;
  }

  startEdit(): void {
    if (!this.selectedOrder) return;
    this.isEditMode = true;
    this.editDeliveryTime = this.selectedOrder.deliveryTime || '';
    this.editNotes = this.selectedOrder.notes || '';
    this.editPaymentMethod = this.selectedOrder.paymentMethod || 'CASH';
    this.editLines = this.selectedOrder.lines.map(l => ({ ...l }));
    this.editSelectedCategory = '';
    this.editProductsByCategory = [];
    this.editSelectedProduct = null;
    this.editQuantity = 1;
    this.editIsPinsa = false;
    this.editNoLactose = false;
    this.editRowNotes = '';
  }

  cancelEdit(): void {
    this.isEditMode = false;
  }

  loadEditProducts(): void {
    if (!this.editSelectedCategory) { this.editProductsByCategory = []; return; }
    this.apiService.getProductsByCategory(this.editSelectedCategory).subscribe(products => {
      this.editProductsByCategory = products.filter(p => p.available !== false);
      this.editSelectedProduct = null;
    });
  }

  addEditProduct(): void {
    if (!this.editSelectedProduct || this.editQuantity < 1) return;
    const surcharge = (this.editIsPinsa ? 1.5 : 0) + (this.editNoLactose ? 1.5 : 0);
    const unitPrice = this.editSelectedProduct.price + surcharge;
    const row: OrderRow = {
      productId: this.editSelectedProduct.id!,
      productName: this.editSelectedProduct.name,
      productCategory: this.editSelectedProduct.category,
      quantity: this.editQuantity,
      unitPrice,
      subtotal: unitPrice * this.editQuantity,
      notes: this.editRowNotes || undefined,
      isPinsa: this.editIsPinsa,
      noLactose: this.editNoLactose,
    };
    this.editLines.push(row);
    this.editSelectedProduct = null;
    this.editQuantity = 1;
    this.editIsPinsa = false;
    this.editNoLactose = false;
    this.editRowNotes = '';
  }

  removeEditLine(index: number): void {
    this.editLines.splice(index, 1);
  }

  get editTotal(): number {
    return this.editLines.reduce((sum, l) => sum + (l.subtotal || 0), 0);
  }

  saveEdit(): void {
    if (!this.selectedOrder || this.editLines.length === 0) return;

    this.isSaving = true;
    const updatedOrder: Order = {
      ...this.selectedOrder,
      deliveryTime: this.editDeliveryTime,
      notes: this.editNotes,
      paymentMethod: this.editPaymentMethod,
      lines: this.editLines,
      total: this.editTotal,
    };

    this.apiService.updateOrder(this.selectedOrder.id!, updatedOrder).subscribe({
      next: (updated) => {
        // Update the order in the list
        const index = this.orders.findIndex(o => o.id === updated.id);
        if (index >= 0) {
          this.orders[index] = updated;
        }
        this.selectedOrder = updated;
        this.isEditMode = false;
        this.isSaving = false;
      },
      error: (err) => {
        console.error('Error updating order', err);
        this.isSaving = false;
      }
    });
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
        this.newStatus = updatedOrder.status || '';
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
      case 'IN_PREPARATION':
        return 'bg-blue-100 text-blue-800';
      case 'READY':
        return 'bg-green-100 text-green-800';
      case 'IN_DELIVERY':
        return 'bg-yellow-100 text-yellow-800';
      case 'DELIVERED':
        return 'bg-gray-100 text-gray-800';
      case 'CANCELLED':
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
