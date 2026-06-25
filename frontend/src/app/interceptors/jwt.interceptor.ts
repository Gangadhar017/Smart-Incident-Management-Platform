import { HttpInterceptorFn } from '@angular/common/http';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('access_token');
  
  // Don't append if it's logging in or refreshing
  if (token && !req.url.includes('/api/v1/auth/')) {
    const cloned = req.clone({
