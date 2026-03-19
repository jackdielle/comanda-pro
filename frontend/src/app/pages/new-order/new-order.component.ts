import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslocoModule } from '@ngneat/transloco';
import { ApiService, Customer, Product, OrderRow, Order } from '../../services/api.service';

export interface Extra {
  id: number;
  name: string;
  price: number;
}

@Component({
  selector: 'app-new-order',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslocoModule],
  templateUrl: './new-order.component.html',
  styleUrls: ['./new-order.component.scss']
})
export class NewOrderComponent implements OnInit {
  // Delivery Type
  deliveryType: 'DOMICILIO' | 'INTERNA' = 'DOMICILIO';
  customerName = '';

  // Customer
  customerId: number | null = null;
  phoneNumber = '';
  searchedCustomers: Customer[] = [];
  selectedCustomer: Customer | null = null;
  newCustomer = false;
  newCustomerForm = {
    phoneNumber: '',
    name: '',
    address: '',
    intercom: '',
    zone: ''
  };

  // Order
  categories = ['CLASSIC', 'SPECIAL', 'PINSE', 'PANOZZI', 'ROLLED', 'FRIED', 'BEVERAGES', 'DESSERTS'];
  selectedCategory = '';
  productsByCategory: Product[] = [];
  selectedProduct: Product | null = null;
  quantity = 1;
  deliveryTime = '';
  orderNotes = '';
  isPinsa = false;
  noLactose = false;
  rowNotes = '';
  paymentMethodIsCreditCard = false; // false = cash, true = credit card

  // Extras (Mocked)
  extras: Extra[] = [
    { id: 1, name: 'Prosciutto', price: 1.50 },
    { id: 2, name: 'Extra Mozzarella', price: 1.00 },
    { id: 3, name: 'Mushrooms', price: 0.80 },
    { id: 4, name: 'Olives', price: 0.80 },
    { id: 5, name: 'Pepperoni', price: 1.20 },
    { id: 6, name: 'Basil', price: 0.50 },
    { id: 7, name: 'Garlic', price: 0.30 },
    { id: 8, name: 'Feta Cheese', price: 1.30 }
  ];
  selectedExtras: Extra[] = [];

  // Summary
  orderRows: OrderRow[] = [];
  orderTotal = 0;
  countClassic = 0;
  countPanozzi = 0;
  countRolled = 0;
  countPinse = 0;

  // UI
  loading = false;
  error: string | null = null;
  successMessage: string | null = null;

  constructor(private apiService: ApiService) { }

  ngOnInit(): void { }

  // ===== PHONE NUMBER VALIDATION =====
  onPhoneKeyDown(event: KeyboardEvent): void {
    // Allow only numeric keys, backspace, delete, tab, enter, arrow keys
    const key = event.key;
    const allowedKeys = ['Backspace', 'Delete', 'Tab', 'Enter', 'ArrowLeft', 'ArrowRight', 'ArrowUp', 'ArrowDown'];
    if (!(/[0-9]/.test(key)) && !allowedKeys.includes(key)) {
      event.preventDefault();
    }
  }

  onPhoneNumberInput(event: any): void {
    // Additional filter for pasted content
    const input = event.target.value;
    this.phoneNumber = input.replace(/[^0-9]/g, '');
  }

  onNewCustomerPhoneKeyDown(event: KeyboardEvent): void {
    // Allow only numeric keys, backspace, delete, tab, enter, arrow keys
    const key = event.key;
    const allowedKeys = ['Backspace', 'Delete', 'Tab', 'Enter', 'ArrowLeft', 'ArrowRight', 'ArrowUp', 'ArrowDown'];
    if (!(/[0-9]/.test(key)) && !allowedKeys.includes(key)) {
      event.preventDefault();
    }
  }

  onNewCustomerPhoneInput(event: any): void {
    // Additional filter for pasted content
    const input = event.target.value;
    this.newCustomerForm.phoneNumber = input.replace(/[^0-9]/g, '');
  }

  // ===== SEARCH CUSTOMER =====
  searchCustomer(): void {
    const searchValue = this.deliveryType === 'INTERNA' ? this.customerName : this.phoneNumber;

    // For phone search, require at least 3 digits
    if (this.deliveryType === 'DOMICILIO' && searchValue.length < 3) {
      this.searchedCustomers = [];
      this.selectedCustomer = null;
      this.newCustomer = false;
      return;
    }

    // For name search, require at least 2 characters
    if (this.deliveryType === 'INTERNA' && searchValue.length < 2) {
      this.searchedCustomers = [];
      return;
    }

    if (this.deliveryType === 'DOMICILIO') {
      // Search for customers by phone number (partial match)
      this.apiService.getAllCustomers().subscribe({
        next: (customers) => {
          // Filter customers whose phone number contains the search value
          this.searchedCustomers = customers.filter(c =>
            c.phoneNumber.includes(searchValue)
          );
          // Clear selection when searching
          if (this.searchedCustomers.length === 0) {
            this.selectedCustomer = null;
            this.newCustomer = true;
          } else {
            this.newCustomer = false;
          }
        },
        error: () => {
          this.searchedCustomers = [];
          this.selectedCustomer = null;
          this.newCustomer = true;
        }
      });
    } else {
      // For internal consumption, create temporary customer
      this.selectedCustomer = {
        id: undefined,
        name: searchValue,
        phoneNumber: '',
        address: '',
        intercom: '',
        zone: 'INTERNO'
      };
      this.newCustomer = false;
      this.searchedCustomers = [];
    }
  }

  selectCustomer(customer: Customer): void {
    this.selectedCustomer = customer;
    this.customerId = customer.id!;

    if (this.deliveryType === 'DOMICILIO') {
      this.phoneNumber = customer.phoneNumber;
    } else {
      this.customerName = customer.name;
    }

    this.searchedCustomers = [];
    this.newCustomer = false;
  }

  // ===== CREATE NEW CUSTOMER =====
  createNewCustomer(): void {
    if (this.deliveryType === 'INTERNA') {
      // For internal consumption, create customer without phone/address
      if (!this.newCustomerForm.name) {
        this.error = 'Enter the customer name';
        return;
      }

      this.selectedCustomer = {
        id: undefined,
        name: this.newCustomerForm.name,
        phoneNumber: '',
        address: '',
        intercom: '',
        zone: 'INTERNO'
      };
      this.customerName = this.newCustomerForm.name;
      this.newCustomer = false;
      this.successMessage = 'Customer created!';
      this.resetNewCustomerForm();
      this.error = null;
    } else {
      // For delivery, create normally
      if (!this.newCustomerForm.phoneNumber || !this.newCustomerForm.name ||
          !this.newCustomerForm.address || !this.newCustomerForm.zone) {
        this.error = 'Fill in all required fields';
        return;
      }

      this.loading = true;
      this.apiService.createCustomer(this.newCustomerForm).subscribe({
        next: (customer) => {
          this.selectedCustomer = customer;
          this.customerId = customer.id!;
          this.phoneNumber = customer.phoneNumber;
          this.newCustomer = false;
          this.loading = false;
          this.successMessage = 'Customer created successfully!';
          this.resetNewCustomerForm();
          this.error = null;
        },
        error: (err) => {
          this.error = 'Error creating customer';
          this.loading = false;
        }
      });
    }
  }

  resetNewCustomerForm(): void {
    this.newCustomerForm = {
      phoneNumber: '',
      name: '',
      address: '',
      intercom: '',
      zone: ''
    };
  }

  // ===== SELECT CATEGORY =====
  selectCategory(): void {
    if (!this.selectedCategory) {
      this.productsByCategory = [];
      return;
    }

    this.apiService.getProductsByCategory(this.selectedCategory).subscribe({
      next: (products) => {
        this.productsByCategory = products;
        this.selectedProduct = null;
      },
      error: (err) => {
        this.error = 'Error loading products';
      }
    });
  }

  // ===== ADD/REMOVE EXTRAS =====
  toggleExtra(extra: Extra): void {
    const index = this.selectedExtras.findIndex(e => e.id === extra.id);
    if (index > -1) {
      this.selectedExtras.splice(index, 1);
    } else {
      this.selectedExtras.push(extra);
    }
  }

  isExtraSelected(extra: Extra): boolean {
    return this.selectedExtras.some(e => e.id === extra.id);
  }

  getExtrasCost(): number {
    return this.selectedExtras.reduce((sum, extra) => sum + extra.price, 0);
  }

  getSelectedExtrasDisplay(): string {
    return this.selectedExtras.map(e => `${e.name} (+€${e.price.toFixed(2)})`).join(', ');
  }

  // ===== ADD PRODUCT TO ORDER =====
  addProduct(): void {
    if (!this.selectedCustomer) {
      this.error = 'Select a customer';
      return;
    }

    if (!this.selectedProduct) {
      this.error = 'Select a product';
      return;
    }

    if (this.quantity < 1) {
      this.error = 'Invalid quantity';
      return;
    }

    // Calculate surcharge for flags
    let surcharge = 0;
    if (this.isPinsa) surcharge += 1.5;
    if (this.noLactose) surcharge += 1.5;

    // Add extras cost per unit
    const extrasCost = this.getExtrasCost();

    const unitPriceWithSurcharge = this.selectedProduct.price + surcharge + extrasCost;
    const subtotal = unitPriceWithSurcharge * this.quantity;

    // Build extras string for notes
    const extrasInfo = this.selectedExtras.length > 0 ? `Extras: ${this.getSelectedExtrasDisplay()}` : '';
    const combinedNotes = [this.rowNotes, extrasInfo].filter(n => n).join(' | ');

    const row: OrderRow = {
      productId: this.selectedProduct.id!,
      productName: this.selectedProduct.name,
      productCategory: this.selectedProduct.category,
      quantity: this.quantity,
      unitPrice: unitPriceWithSurcharge,
      subtotal: subtotal,
      notes: combinedNotes,
      isPinsa: this.isPinsa,
      noLactose: this.noLactose
    };

    this.orderRows.push(row);
    this.calculateTotal();
    this.updateCounters(row);
    this.resetProductForm();
    this.error = null;
  }

  resetProductForm(): void {
    this.selectedProduct = null;
    this.quantity = 1;
    this.rowNotes = '';
    this.isPinsa = false;
    this.noLactose = false;
    this.selectedExtras = [];
  }

  removeRow(index: number): void {
    const row = this.orderRows[index];
    this.orderRows.splice(index, 1);
    this.calculateTotal();
    this.subtractCounters(row);
  }

  // ===== CALCULATIONS =====
  calculateTotal(): void {
    this.orderTotal = this.orderRows.reduce((sum, row) => sum + (row.subtotal || 0), 0);
  }

  updateCounters(row: OrderRow): void {
    if (row.isPinsa && row.productCategory === 'CLASSICHE') {
      this.countPinse += row.quantity;
    } else {
      switch (row.productCategory) {
        case 'CLASSICHE':
          this.countClassic += row.quantity;
          break;
        case 'PANOZZI':
          this.countPanozzi += row.quantity;
          break;
        case 'ARROTOLATI':
          this.countRolled += row.quantity;
          break;
        case 'PINSE':
          this.countPinse += row.quantity;
          break;
      }
    }
  }

  subtractCounters(row: OrderRow): void {
    if (row.isPinsa && row.productCategory === 'CLASSICHE') {
      this.countPinse -= row.quantity;
    } else {
      switch (row.productCategory) {
        case 'CLASSICHE':
          this.countClassic -= row.quantity;
          break;
        case 'PANOZZI':
          this.countPanozzi -= row.quantity;
          break;
        case 'ARROTOLATI':
          this.countRolled -= row.quantity;
          break;
        case 'PINSE':
          this.countPinse -= row.quantity;
          break;
      }
    }
  }

  // ===== SAVE ORDER =====
  saveOrder(): void {
    if (!this.selectedCustomer) {
      this.error = 'Select a customer';
      return;
    }

    if (this.orderRows.length === 0) {
      this.error = 'Add at least one product';
      return;
    }

    const order: Order = {
      customer: this.selectedCustomer,
      lines: this.orderRows,
      deliveryTime: this.deliveryTime,
      notes: this.orderNotes,
      status: 'IN_PREPARAZIONE',
      paymentMethod: this.paymentMethodIsCreditCard ? 'CREDIT_CARD' : 'CASH'
    };

    this.loading = true;
    this.apiService.createOrder(order).subscribe({
      next: (createdOrder) => {
        this.successMessage = `Order #${createdOrder.id} created successfully!`;
        this.resetForm();
        this.loading = false;
        this.error = null;
      },
      error: (err) => {
        this.error = 'Error creating order';
        this.loading = false;
      }
    });
  }

  resetForm(): void {
    this.selectedCustomer = null;
    this.customerId = null;
    this.phoneNumber = '';
    this.customerName = '';
    this.orderRows = [];
    this.orderTotal = 0;
    this.countClassic = 0;
    this.countPanozzi = 0;
    this.countRolled = 0;
    this.countPinse = 0;
    this.deliveryTime = '';
    this.orderNotes = '';
    this.paymentMethodIsCreditCard = false;
    this.selectedCategory = '';
    this.productsByCategory = [];
    this.resetProductForm();
  }
}
