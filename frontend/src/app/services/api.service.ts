import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';

const API_URL = environment.apiUrl;

export interface Customer {
  id?: number;
  phoneNumber: string;
  name: string;
  address: string;
  intercom?: string;
  zone: string;
}

export interface Product {
  id?: number;
  name: string;
  price: number;
  category: string;
  available?: boolean;
}

export interface OrderRow {
  id?: number;
  productId: number;
  productName?: string;
  productCategory?: string;
  quantity: number;
  unitPrice?: number;
  subtotal?: number;
  notes?: string;
  isPinsa?: boolean;
  noLactose?: boolean;
}

export interface Order {
  id?: number;
  customer: Customer;
  lines: OrderRow[];
  total?: number;
  status?: string;
  deliveryTime?: string;
  notes?: string;
  countClassic?: number;
  countPanozzi?: number;
  countRolled?: number;
  countPinse?: number;
  createdAt?: number;
}

export interface OrdersSummary {
  totalOrders: number;
  totalRevenue: number;
  totalClassic: number;
  totalPanozzi: number;
  totalRolled: number;
  totalPinse: number;
  ordersByStatus: { [key: string]: number };
}

export interface DailyOrderSummary {
  date: string;
  totalOrders: number;
  totalRevenue: number;
  totalClassic: number;
  totalPanozzi: number;
  totalRolled: number;
  totalPinse: number;
}

export interface AppUser {
  id: number;
  username: string;
  role: string;
  enabled: boolean;
  createdAt: string;
}

export interface CreateUserRequest {
  username: string;
  password: string;
  role: string;
}

export interface ResetPasswordRequest {
  newPassword: string;
}

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  constructor(private http: HttpClient) { }

  // ===== CUSTOMERS =====
  createCustomer(customer: Customer): Observable<Customer> {
    return this.http.post<Customer>(`${API_URL}/customers`, customer).pipe(
      catchError(this.handleError)
    );
  }

  getCustomer(id: number): Observable<Customer> {
    return this.http.get<Customer>(`${API_URL}/customers/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  getCustomerByPhone(phoneNumber: string): Observable<Customer> {
    return this.http.get<Customer>(`${API_URL}/customers/phone/${phoneNumber}`).pipe(
      catchError(this.handleError)
    );
  }

  getAllCustomers(): Observable<Customer[]> {
    return this.http.get<Customer[]>(`${API_URL}/customers`).pipe(
      catchError(this.handleError)
    );
  }

  searchCustomer(name: string): Observable<Customer[]> {
    return this.http.get<Customer[]>(`${API_URL}/customers/find/${name}`).pipe(
      catchError(this.handleError)
    );
  }

  updateCustomer(id: number, customer: Customer): Observable<Customer> {
    return this.http.put<Customer>(`${API_URL}/customers/${id}`, customer).pipe(
      catchError(this.handleError)
    );
  }

  deleteCustomer(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/customers/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  // ===== PRODUCTS =====
  createProduct(product: Product): Observable<Product> {
    return this.http.post<Product>(`${API_URL}/products`, product).pipe(
      catchError(this.handleError)
    );
  }

  getProduct(id: number): Observable<Product> {
    return this.http.get<Product>(`${API_URL}/products/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  getAllProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(`${API_URL}/products`).pipe(
      catchError(this.handleError)
    );
  }

  getProductsByCategory(category: string): Observable<Product[]> {
    return this.http.get<Product[]>(`${API_URL}/products/category/${category}`).pipe(
      catchError(this.handleError)
    );
  }

  updateProduct(id: number, product: Product): Observable<Product> {
    return this.http.put<Product>(`${API_URL}/products/${id}`, product).pipe(
      catchError(this.handleError)
    );
  }

  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/products/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  // ===== ORDERS =====
  createOrder(order: Order): Observable<Order> {
    return this.http.post<Order>(`${API_URL}/orders`, order).pipe(
      catchError(this.handleError)
    );
  }

  getOrder(id: number): Observable<Order> {
    return this.http.get<Order>(`${API_URL}/orders/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  getAllOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(`${API_URL}/orders`).pipe(
      catchError(this.handleError)
    );
  }

  getOrdersByCustomer(customerId: number): Observable<Order[]> {
    return this.http.get<Order[]>(`${API_URL}/orders/customer/${customerId}`).pipe(
      catchError(this.handleError)
    );
  }

  getOrdersByStatus(status: string): Observable<Order[]> {
    return this.http.get<Order[]>(`${API_URL}/orders/status/${status}`).pipe(
      catchError(this.handleError)
    );
  }

  updateOrderStatus(id: number, status: string): Observable<Order> {
    return this.http.put<Order>(`${API_URL}/orders/${id}/status/${status}`, {}).pipe(
      catchError(this.handleError)
    );
  }

  deleteOrder(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/orders/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  getOrdersSummary(): Observable<OrdersSummary> {
    return this.http.get<OrdersSummary>(`${API_URL}/orders/summary/totals`).pipe(
      catchError(this.handleError)
    );
  }

  getOrdersSummaryByDateRange(startDate: string, endDate: string): Observable<DailyOrderSummary[]> {
    return this.http.get<DailyOrderSummary[]>(`${API_URL}/orders/summary/by-date-range`, {
      params: { startDate, endDate }
    }).pipe(
      catchError(this.handleError)
    );
  }

  // ===== USERS =====
  getAllUsers(): Observable<AppUser[]> {
    return this.http.get<AppUser[]>(`${API_URL}/users`).pipe(
      catchError(this.handleError)
    );
  }

  createUser(request: CreateUserRequest): Observable<AppUser> {
    return this.http.post<AppUser>(`${API_URL}/users`, request).pipe(
      catchError(this.handleError)
    );
  }

  toggleUser(userId: number): Observable<AppUser> {
    return this.http.put<AppUser>(`${API_URL}/users/${userId}/toggle-enabled`, {}).pipe(
      catchError(this.handleError)
    );
  }

  deleteUser(userId: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/users/${userId}`).pipe(
      catchError(this.handleError)
    );
  }

  resetUserPassword(userId: number, request: ResetPasswordRequest): Observable<void> {
    return this.http.put<void>(`${API_URL}/users/${userId}/reset-password`, request).pipe(
      catchError(this.handleError)
    );
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'Unknown error';
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Error: ${error.error.message}`;
    } else {
      errorMessage = `Server error: ${error.status} - ${error.message}`;
    }
    console.error(errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
