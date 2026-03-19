import { Routes } from '@angular/router';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { NewOrderComponent } from './pages/new-order/new-order.component';
import { ViewOrdersComponent } from './pages/view-orders/view-orders.component';
import { ManageCustomersComponent } from './pages/manage-customers/manage-customers.component';
import { ManageProductsComponent } from './pages/manage-products/manage-products.component';
import { LoginComponent } from './pages/login/login.component';
import { ProfileComponent } from './pages/profile/profile.component';
import { AdminUsersComponent } from './pages/admin-users/admin-users.component';
import { ReportsComponent } from './pages/reports/reports.component';
import { authGuard } from './guards/auth.guard';
import { adminGuard } from './guards/admin.guard';

export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent
  },
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [authGuard]
  },
  {
    path: '',
    component: DashboardComponent,
    canActivate: [authGuard]
  },
  {
    path: 'new-order',
    component: NewOrderComponent,
    canActivate: [authGuard]
  },
  {
    path: 'orders',
    component: ViewOrdersComponent,
    canActivate: [authGuard]
  },
  {
    path: 'customers',
    component: ManageCustomersComponent,
    canActivate: [authGuard]
  },
  {
    path: 'products',
    component: ManageProductsComponent,
    canActivate: [authGuard]
  },
  {
    path: 'profile',
    component: ProfileComponent,
    canActivate: [authGuard]
  },
  {
    path: 'admin/users',
    component: AdminUsersComponent,
    canActivate: [authGuard, adminGuard]
  },
  {
    path: 'reports',
    component: ReportsComponent,
    canActivate: [authGuard, adminGuard]
  },
  {
    path: '**',
    redirectTo: ''
  }
];
