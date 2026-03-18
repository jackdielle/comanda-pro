import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { routes } from './app/app.routes';
import { authInterceptor } from './app/interceptors/auth.interceptor';
import { provideTransloco } from '@ngneat/transloco';
import { TranslocoHttpLoader } from './app/transloco.loader';
import { translocoConfig } from './app/transloco-root.config';

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
    provideTransloco({
      config: translocoConfig,
      loader: TranslocoHttpLoader
    })
  ]
}).catch(err => console.error(err));
