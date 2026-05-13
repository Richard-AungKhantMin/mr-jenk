import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { SessionService } from './session.service';
import { environment } from '../../environments/environment';
//hello
describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let sessionService: SessionService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService, SessionService]
    });

    service = TestBed.inject(AuthService);
    sessionService = TestBed.inject(SessionService);
    httpMock = TestBed.inject(HttpTestingController);
    
    // Clear session before each test
    sessionService.clear();
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('login', () => {
    it('should send login credentials to identity service', (done) => {
      const credentials = { email: 'test@example.com', password: 'password123' };

      service.login(credentials).subscribe((response) => {
        expect(response.token).toBeTruthy();
        done();
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(credentials);
      req.flush({ token: 'jwt-token-123', role: 'CLIENT', email: 'test@example.com', id: 'user-123' });
    });

    it('should handle login error', (done) => {
      const credentials = { email: 'test@example.com', password: 'wrongpassword' };

      service.login(credentials).subscribe(
        () => fail('should have failed'),
        (error) => {
          expect(error.status).toBe(401);
          done();
        }
      );

      const req = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
      req.flush({ error: 'Invalid credentials' }, { status: 401, statusText: 'Unauthorized' });
    });
  });

  describe('register', () => {
    it('should register a new user', (done) => {
      const userData = {
        name: 'John Doe',
        email: 'john@example.com',
        password: 'password123',
        role: 'BUYER'
      };

      service.register(userData).subscribe((response) => {
        expect(response.email).toBe('john@example.com');
        expect(response.name).toBe('John Doe');
        done();
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/auth/register`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(userData);
      req.flush({ id: 'user-123', ...userData });
    });

    it('should handle registration error', (done) => {
      const userData = {
        name: 'John Doe',
        email: 'john@example.com',
        password: 'password123',
        role: 'BUYER'
      };

      service.register(userData).subscribe(
        () => fail('should have failed'),
        (error) => {
          expect(error.status).toBe(400);
          done();
        }
      );

      const req = httpMock.expectOne(`${environment.apiUrl}/auth/register`);
      req.flush({ error: 'Email already exists' }, { status: 400, statusText: 'Bad Request' });
    });
  });

  describe('logout', () => {
    it('should clear session on logout', () => {
      spyOn(sessionService, 'clear');
      service.logout();
      expect(sessionService.clear).toHaveBeenCalled();
    });
  });

  describe('Token Management', () => {
    it('should set and get token', () => {
      const token = 'jwt-token-123';
      service.setToken(token);
      expect(service.getToken()).toBe(token);
    });

    it('should set and get user', () => {
      const user = {
        id: 'user-123',
        name: 'John Doe',
        email: 'john@example.com',
        role: 'CLIENT' as const,
        token: 'jwt-token-123'
      };
      service.setUser(user);
      expect(service.getUser()).toEqual(user);
    });

    it('should check authentication status', () => {
      expect(service.isAuthenticated()).toBeFalsy();
      service.setToken('jwt-token-123');
      expect(service.isAuthenticated()).toBeTruthy();
    });
  });

  describe('getCurrentUser', () => {
    it('should return current user from session', () => {
      const user = {
        id: 'user-123',
        name: 'John Doe',
        email: 'john@example.com',
        role: 'CLIENT' as const,
        token: 'jwt-token-123'
      };
      service.setUser(user);
      expect(service.getCurrentUser()).toEqual(user);
    });

    it('should return null if no user is set', () => {
      sessionService.clear();
      expect(service.getCurrentUser()).toBeNull();
    });
  });
});
