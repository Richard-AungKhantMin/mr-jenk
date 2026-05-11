import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ProductService } from './product.service';
import { environment } from '../../environments/environment';

describe('ProductService', () => {
  let service: ProductService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ProductService]
    });

    service = TestBed.inject(ProductService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('getAllProducts', () => {
    it('should fetch all products', (done) => {
      const mockProducts = [
        { id: 'prod-1', name: 'Product 1', price: 99.99 } as any,
        { id: 'prod-2', name: 'Product 2', price: 149.99 } as any
      ];

      service.getAllProducts().subscribe((products: any[]) => {
        expect(products.length).toBe(2);
        expect(products[0].name).toBe('Product 1');
        done();
      });

      const req = httpMock.expectOne(req => req.url.includes('/products'));
      expect(req.request.method).toBe('GET');
      req.flush(mockProducts);
    });

    it('should handle error when fetching products', (done) => {
      service.getAllProducts().subscribe(
        () => fail('should have failed'),
        (error: any) => {
          expect(error.status).toBe(500);
          done();
        }
      );

      const req = httpMock.expectOne(req => req.url.includes('/products'));
      req.flush({ error: 'Server error' }, { status: 500, statusText: 'Internal Server Error' });
    });
  });

  describe('getProduct', () => {
    it('should fetch a single product by id', (done) => {
      const mockProduct = {
        id: 'prod-1',
        name: 'Product 1',
        price: 99.99,
        description: 'Test product'
      } as any;

      service.getProduct('prod-1').subscribe((product: any) => {
        expect(product.id).toBe('prod-1');
        expect(product.name).toBe('Product 1');
        done();
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/products/prod-1`);
      expect(req.request.method).toBe('GET');
      req.flush(mockProduct);
    });

    it('should handle 404 error when product not found', (done) => {
      service.getProduct('invalid-id').subscribe(
        () => fail('should have failed'),
        (error: any) => {
          expect(error.status).toBe(404);
          done();
        }
      );

      const req = httpMock.expectOne(`${environment.apiUrl}/products/invalid-id`);
      req.flush({ error: 'Product not found' }, { status: 404, statusText: 'Not Found' });
    });
  });

  describe('createProduct', () => {
    it('should create a new product', (done) => {
      const newProduct = {
        id: '',
        name: 'New Product',
        description: 'A new product',
        price: 199.99,
        quantity: 5,
        sellerId: 'seller-1'
      } as any;

      const createdProduct = { ...newProduct, id: 'prod-new' };

      service.createProduct(newProduct).subscribe((product: any) => {
        expect(product.id).toBe('prod-new');
        expect(product.name).toBe('New Product');
        done();
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/products`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(newProduct);
      req.flush(createdProduct);
    });

    it('should handle validation error on create', (done) => {
      const invalidProduct = { name: '', price: -50 };

      service.createProduct(invalidProduct as any).subscribe(
        () => fail('should have failed'),
        (error) => {
          expect(error.status).toBe(400);
          done();
        }
      );

      const req = httpMock.expectOne(`${environment.apiUrl}/products`);
      req.flush({ error: 'Validation failed' }, { status: 400, statusText: 'Bad Request' });
    });
  });

  describe('updateProduct', () => {
    it('should update an existing product', (done) => {
      const updatedProduct = {
        id: 'prod-1',
        name: 'Updated Product',
        price: 199.99
      };

      service.updateProduct('prod-1', updatedProduct as any).subscribe((product) => {
        expect(product.name).toBe('Updated Product');
        done();
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/products/prod-1`);
      expect(req.request.method).toBe('PUT');
      req.flush(updatedProduct);
    });

    it('should handle unauthorized error on update', (done) => {
      service.updateProduct('prod-1', {} as any).subscribe(
        () => fail('should have failed'),
        (error) => {
          expect(error.status).toBe(403);
          done();
        }
      );

      const req = httpMock.expectOne(`${environment.apiUrl}/products/prod-1`);
      req.flush({ error: 'Unauthorized' }, { status: 403, statusText: 'Forbidden' });
    });
  });

  describe('deleteProduct', () => {
    it('should delete a product', (done) => {
      service.deleteProduct('prod-1').subscribe(() => {
        done();
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/products/prod-1`);
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });

    it('should handle error on delete', (done) => {
      service.deleteProduct('prod-1').subscribe(
        () => fail('should have failed'),
        (error) => {
          expect(error.status).toBe(404);
          done();
        }
      );

      const req = httpMock.expectOne(`${environment.apiUrl}/products/prod-1`);
      req.flush({ error: 'Product not found' }, { status: 404, statusText: 'Not Found' });
    });
  });
});
