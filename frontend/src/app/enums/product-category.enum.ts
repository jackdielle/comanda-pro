export enum ProductCategory {
  CLASSIC = 'CLASSIC',
  SPECIAL = 'SPECIAL',
  PINSE = 'PINSE',
  PANOZZI = 'PANOZZI',
  ROLLED = 'ROLLED',
  FRIED = 'FRIED',
  BEVERAGES = 'BEVERAGES',
  DESSERTS = 'DESSERTS'
}

export const PRODUCT_CATEGORY_LABELS: { [key in ProductCategory]: string } = {
  [ProductCategory.CLASSIC]: 'products.CLASSIC',
  [ProductCategory.SPECIAL]: 'products.SPECIAL',
  [ProductCategory.PINSE]: 'products.PINSE',
  [ProductCategory.PANOZZI]: 'products.PANOZZI',
  [ProductCategory.ROLLED]: 'products.ROLLED',
  [ProductCategory.FRIED]: 'products.FRIED',
  [ProductCategory.BEVERAGES]: 'products.BEVERAGES',
  [ProductCategory.DESSERTS]: 'products.DESSERTS'
};

export function getProductCategoryLabel(category?: string): string {
  return PRODUCT_CATEGORY_LABELS[category as ProductCategory] || '';
}

export const PRODUCT_CATEGORIES = Object.values(ProductCategory);
