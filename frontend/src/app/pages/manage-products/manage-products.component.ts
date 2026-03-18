import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslocoModule } from '@ngneat/transloco';
import { ApiService, Product } from '../../services/api.service';

@Component({
  selector: 'app-manage-products',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslocoModule],
  templateUrl: './manage-products.component.html',
  styleUrls: ['./manage-products.component.scss']
})
export class ManageProductsComponent implements OnInit {
  products: Product[] = [];
  categories = ['CLASSIC', 'SPECIAL', 'PINSE', 'PANOZZI', 'ROLLED', 'FRIED', 'BEVERAGES', 'DESSERTS'];
  loading = true;
  error: string | null = null;
  successMessage: string | null = null;

  formVisible = false;
  productBeingEdited: Product | null = null;
  formData = this.createEmptyForm();

  constructor(private apiService: ApiService) { }

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.loading = true;
    this.apiService.getAllProducts().subscribe({
      next: (products) => {
        this.products = products;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error loading products';
        this.loading = false;
      }
    });
  }

  createEmptyForm(): Product {
    return {
      name: '',
      price: 0,
      category: '',
      available: true
    };
  }

  openForm(product?: Product): void {
    if (product) {
      this.productBeingEdited = product;
      this.formData = { ...product };
    } else {
      this.productBeingEdited = null;
      this.formData = this.createEmptyForm();
    }
    this.formVisible = true;
  }

  closeForm(): void {
    this.formVisible = false;
    this.formData = this.createEmptyForm();
    this.productBeingEdited = null;
  }

  saveProduct(): void {
    if (!this.formData.name || !this.formData.category || this.formData.price <= 0) {
      this.error = 'Fill in all fields with valid values';
      return;
    }

    if (this.productBeingEdited) {
      this.apiService.updateProduct(this.productBeingEdited.id!, this.formData).subscribe({
        next: (updatedProduct) => {
          const index = this.products.findIndex(p => p.id === updatedProduct.id);
          if (index > -1) {
            this.products[index] = updatedProduct;
          }
          this.successMessage = 'Product updated successfully!';
          this.closeForm();
        },
        error: (err) => {
          this.error = 'Error updating product';
        }
      });
    } else {
      this.apiService.createProduct(this.formData).subscribe({
        next: (newProduct) => {
          this.products.unshift(newProduct);
          this.successMessage = 'Product created successfully!';
          this.closeForm();
        },
        error: (err) => {
          this.error = err.message;
        }
      });
    }
  }

  deleteProduct(id: number | undefined): void {
    if (!id || !confirm('Are you sure you want to delete this product?')) {
      return;
    }

    this.apiService.deleteProduct(id).subscribe({
      next: () => {
        this.products = this.products.filter(p => p.id !== id);
        this.successMessage = 'Product deleted successfully!';
      },
      error: (err) => {
        this.error = 'Error deleting product';
      }
    });
  }

  getProductsByCategory(category: string): Product[] {
    return this.products.filter(p => p.category === category);
  }
}
