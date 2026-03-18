import { TranslocoConfig } from '@ngneat/transloco';
import {isDevMode} from "@angular/core";

export const translocoConfig: Partial<TranslocoConfig> = {
  availableLangs: ['en', 'it'],
  defaultLang: 'it',
  prodMode: !isDevMode(),
};
