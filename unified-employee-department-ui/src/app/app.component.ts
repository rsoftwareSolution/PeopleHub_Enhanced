import { CommonModule } from '@angular/common';
import {
  Component,
  DestroyRef,
  computed,
  inject,
  signal
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import {
  EMPTY,
  Subject,
  catchError,
  debounceTime,
  distinctUntilChanged,
  finalize,
  forkJoin,
  switchMap
} from 'rxjs';

import { ApiService } from './core/api.service';
import { AuthService } from './core/auth.service';
import { InactivityService } from './core/inactivity.service';

import {
  Department,
  Employee,
  EmployeePage,
  EmployeeSearchResponse
} from './models/models';

type View = 'dashboard' | 'employees' | 'departments' | 'integration';
type EmployeeSortField = 'name' | 'email' | 'id' | 'departmentId';
type SortDirection = 'asc' | 'desc';

type Notice = {
  type: 'success' | 'error';
  text: string;
};

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule
  ],
  templateUrl: './app.component.html'
})
export class AppComponent {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(ApiService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly inactivity = inject(InactivityService);

  readonly auth = inject(AuthService);

  // ---------------------------------------------------------------------------
  // Application state
  // ---------------------------------------------------------------------------

  readonly view = signal<View>('dashboard');

  readonly employees = signal<Employee[]>([]);
  readonly departments = signal<Department[]>([]);

  readonly busy = signal(false);

  readonly dashboardLoading = signal(false);
  readonly employeeLoading = signal(false);

  // Kept for compatibility with the existing HTML loading indicator.
  readonly loading = computed(
    () => this.dashboardLoading() || this.employeeLoading()
  );

  readonly notice = signal<Notice | null>(null);

  readonly showEmployeeModal = signal(false);
  readonly showDepartmentModal = signal(false);

  readonly editingEmployeeId = signal<number | null>(null);
  readonly editingDepartmentId = signal<number | null>(null);

  readonly lookupResult = signal<Department | null>(null);

  // ---------------------------------------------------------------------------
  // Employee directory state
  // ---------------------------------------------------------------------------

  readonly employeeSearch = signal('');

  readonly directoryEmployees = signal<Employee[]>([]);

  readonly employeeDepartmentFilter = signal<number | null>(null);

  readonly employeeSortField = signal<EmployeeSortField>('name');

  readonly employeeSortDirection = signal<SortDirection>('asc');

  readonly employeePage = signal(0);
  readonly employeePageSize = signal(20);

  readonly employeeTotalPages = signal(0);
  readonly employeeTotalElements = signal(0);

  // ---------------------------------------------------------------------------
  // Department directory state
  // ---------------------------------------------------------------------------

  readonly departmentSearch = signal('');

  readonly filteredEmployees = computed(
    () => this.directoryEmployees()
  );

  readonly filteredDepartments = computed(() => {
    const query = this.departmentSearch().trim().toLowerCase();

    if (!query) {
      return this.departments();
    }

    return this.departments().filter(department => {
      const searchableText =
        `${department.name} ${department.location}`.toLowerCase();

      return searchableText.includes(query);
    });
  });

  // ---------------------------------------------------------------------------
  // Reactive forms
  // ---------------------------------------------------------------------------

  readonly loginForm = this.fb.nonNullable.group({
    username: ['admin', Validators.required],
    password: ['admin123', Validators.required]
  });

  readonly employeeForm = this.fb.nonNullable.group({
    name: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    departmentId: [1, [Validators.required, Validators.min(1)]]
  });

  readonly departmentForm = this.fb.nonNullable.group({
    name: ['', Validators.required],
    location: ['', Validators.required]
  });

  readonly lookupForm = this.fb.nonNullable.group({
    departmentId: [1, [Validators.required, Validators.min(1)]]
  });

  // ---------------------------------------------------------------------------
  // RxJS request subjects
  // ---------------------------------------------------------------------------

  private readonly employeePageRequest$ = new Subject<void>();

  private readonly employeeSearchInput$ = new Subject<string>();

  private noticeTimeout?: ReturnType<typeof setTimeout>;

  constructor() {
    this.initializeEmployeeSearch();
    this.initializeEmployeePageRequests();

    this.destroyRef.onDestroy(() => {
      if (this.noticeTimeout) {
        clearTimeout(this.noticeTimeout);
      }
    });

    if (this.auth.isLoggedIn()) {
      this.startInactivityMonitoring();
      this.loadAll();
      this.loadEmployeePage();
    }
  }

  // ---------------------------------------------------------------------------
  // Initialization
  // ---------------------------------------------------------------------------

  private initializeEmployeeSearch(): void {
    this.employeeSearchInput$
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.employeePage.set(0);
        this.loadEmployeePage();
      });
  }

  private initializeEmployeePageRequests(): void {
    this.employeePageRequest$
      .pipe(
        switchMap(() => {
          if (!this.auth.isLoggedIn()) {
            return EMPTY;
          }

          this.employeeLoading.set(true);

          return this.api.searchEmployees({
            q: this.employeeSearch().trim(),
            departmentId:
              this.employeeDepartmentFilter() ?? undefined,
            page: this.employeePage(),
            size: this.employeePageSize(),
            sort:
              `${this.employeeSortField()},${this.employeeSortDirection()}`
          }).pipe(
            catchError(error => {
              this.flash(
                'error',
                this.message(error, 'Unable to load employees.')
              );

              return EMPTY;
            }),
            finalize(() => {
              this.employeeLoading.set(false);
            })
          );
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(response => {
        this.applyEmployeeSearchResponse(response);
      });
  }

  // ---------------------------------------------------------------------------
  // Authentication
  // ---------------------------------------------------------------------------

  login(): void {
    if (this.loginForm.invalid || this.busy()) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.busy.set(true);

    const { username, password } = this.loginForm.getRawValue();

    this.auth.login(username, password)
      .pipe(
        finalize(() => this.busy.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: () => {
          this.startInactivityMonitoring();

          this.view.set('dashboard');

          this.flash(
            'success',
            `Signed in as ${this.auth.role() ?? 'user'}.`
          );

          this.loadAll();
          this.loadEmployeePage();
        },
        error: error => {
          this.flash(
            'error',
            this.message(
              error,
              'Login failed. Check your credentials and Employee Service.'
            )
          );
        }
      });
  }

  logout(automatic = false): void {
    this.inactivity.stop();
    this.auth.logout();

    this.employees.set([]);
    this.departments.set([]);
    this.directoryEmployees.set([]);

    this.employeeTotalElements.set(0);
    this.employeeTotalPages.set(0);
    this.employeePage.set(0);

    this.lookupResult.set(null);

    this.showEmployeeModal.set(false);
    this.showDepartmentModal.set(false);

    this.view.set('dashboard');

    this.flash(
      'success',
      automatic
        ? 'You were signed out after 30 minutes of inactivity.'
        : 'You have been signed out.'
    );
  }

  private startInactivityMonitoring(): void {
    this.inactivity.start(() => this.logout(true));
  }

  // ---------------------------------------------------------------------------
  // Dashboard data
  // ---------------------------------------------------------------------------

  loadAll(): void {
    if (!this.auth.isLoggedIn()) {
      return;
    }

    this.dashboardLoading.set(true);

    forkJoin({
      employees: this.api.employees(),
      departments: this.api.departments()
    })
      .pipe(
        finalize(() => this.dashboardLoading.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: data => {
          this.employees.set(data.employees);
          this.departments.set(data.departments);
        },
        error: error => {
          this.flash(
            'error',
            this.message(
              error,
              'Unable to load dashboard data. Confirm both services and CORS settings.'
            )
          );
        }
      });
  }

  refreshData(): void {
    this.loadAll();

    if (this.auth.isLoggedIn()) {
      this.loadEmployeePage();
    }
  }

  // ---------------------------------------------------------------------------
  // Employee search, sorting and pagination
  // ---------------------------------------------------------------------------

  loadEmployeePage(): void {
    if (!this.auth.isLoggedIn()) {
      return;
    }

    this.employeePageRequest$.next();
  }

  private applyEmployeeSearchResponse(
    response: EmployeeSearchResponse
  ): void {
    // Supports APIs that return a plain Employee[].
    if (Array.isArray(response)) {
      this.directoryEmployees.set(response);

      this.employeeTotalElements.set(response.length);
      this.employeeTotalPages.set(response.length > 0 ? 1 : 0);

      this.employeePage.set(0);

      return;
    }

    // Supports Spring Boot Page<Employee> responses.
    const page = response as EmployeePage;

    const content = Array.isArray(page.content)
      ? page.content
      : [];

    const totalElements = Number(page.totalElements ?? content.length);
    const totalPages = Number(page.totalPages ?? 0);
    const currentPage = Number(page.number ?? this.employeePage());

    this.directoryEmployees.set(content);

    this.employeeTotalElements.set(
      Number.isFinite(totalElements) ? totalElements : content.length
    );

    this.employeeTotalPages.set(
      Number.isFinite(totalPages) ? totalPages : 0
    );

    this.employeePage.set(
      Number.isFinite(currentPage) ? currentPage : 0
    );
  }

  onEmployeeSearch(value: string): void {
    this.employeeSearch.set(value);
    this.employeeSearchInput$.next(value);
  }

  onEmployeeDepartmentChange(value: string): void {
    const departmentId = value ? Number(value) : null;

    this.employeeDepartmentFilter.set(
      departmentId !== null &&
      Number.isInteger(departmentId) &&
      departmentId > 0
        ? departmentId
        : null
    );

    this.employeePage.set(0);
    this.loadEmployeePage();
  }

  onEmployeeSortFieldChange(value: string): void {
    const allowedFields: EmployeeSortField[] = [
      'name',
      'email',
      'id',
      'departmentId'
    ];

    if (!allowedFields.includes(value as EmployeeSortField)) {
      return;
    }

    this.employeeSortField.set(value as EmployeeSortField);

    this.employeePage.set(0);
    this.loadEmployeePage();
  }

  onEmployeeSortDirectionChange(value: string): void {
    if (value !== 'asc' && value !== 'desc') {
      return;
    }

    this.employeeSortDirection.set(value);

    this.employeePage.set(0);
    this.loadEmployeePage();
  }

  changeEmployeePage(page: number): void {
    if (
      !Number.isInteger(page) ||
      page < 0 ||
      page >= this.employeeTotalPages()
    ) {
      return;
    }

    this.employeePage.set(page);
    this.loadEmployeePage();
  }

  // ---------------------------------------------------------------------------
  // Employee CRUD
  // ---------------------------------------------------------------------------

  openEmployee(employee?: Employee): void {
    if (!this.auth.isAdmin()) {
      return;
    }

    this.editingEmployeeId.set(employee?.id ?? null);

    this.employeeForm.reset({
      name: employee?.name ?? '',
      email: employee?.email ?? '',
      departmentId:
        employee?.departmentId ??
        this.departments()[0]?.id ??
        1
    });

    this.showEmployeeModal.set(true);
  }

  saveEmployee(): void {
    if (!this.auth.isAdmin() || this.busy()) {
      return;
    }

    if (this.employeeForm.invalid) {
      this.employeeForm.markAllAsTouched();
      return;
    }

    this.busy.set(true);

    const value = this.employeeForm.getRawValue();
    const id = this.editingEmployeeId();

    const request = id
      ? this.api.updateEmployee(id, value)
      : this.api.createEmployee(value);

    request
      .pipe(
        finalize(() => this.busy.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: () => {
          this.showEmployeeModal.set(false);

          this.flash(
            'success',
            id ? 'Employee updated.' : 'Employee created.'
          );

          this.loadAll();
          this.loadEmployeePage();
        },
        error: error => {
          this.flash(
            'error',
            this.message(error, 'Employee operation failed.')
          );
        }
      });
  }

  removeEmployee(employee: Employee): void {
    if (
      !employee.id ||
      !this.auth.isAdmin() ||
      this.busy()
    ) {
      return;
    }

    const confirmed = confirm(
      `Are you sure you want to delete ${employee.name}?`
    );

    if (!confirmed) {
      return;
    }

    this.busy.set(true);

    this.api.deleteEmployee(employee.id)
      .pipe(
        finalize(() => this.busy.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: () => {
          this.flash('success', 'Employee deleted.');

          this.loadAll();
          this.loadEmployeePage();
        },
        error: error => {
          this.flash(
            'error',
            this.message(error, 'Unable to delete employee.')
          );
        }
      });
  }

  // ---------------------------------------------------------------------------
  // Department CRUD
  // ---------------------------------------------------------------------------

  openDepartment(department?: Department): void {
    if (!this.auth.isAdmin()) {
      return;
    }

    this.editingDepartmentId.set(department?.id ?? null);

    this.departmentForm.reset({
      name: department?.name ?? '',
      location: department?.location ?? ''
    });

    this.showDepartmentModal.set(true);
  }

  saveDepartment(): void {
    if (!this.auth.isAdmin() || this.busy()) {
      return;
    }

    if (this.departmentForm.invalid) {
      this.departmentForm.markAllAsTouched();
      return;
    }

    this.busy.set(true);

    const value = this.departmentForm.getRawValue();
    const id = this.editingDepartmentId();

    const request = id
      ? this.api.updateDepartment(id, value)
      : this.api.createDepartment(value);

    request
      .pipe(
        finalize(() => this.busy.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: () => {
          this.showDepartmentModal.set(false);

          this.flash(
            'success',
            id ? 'Department updated.' : 'Department created.'
          );

          this.loadAll();
          this.loadEmployeePage();
        },
        error: error => {
          this.flash(
            'error',
            this.message(error, 'Department operation failed.')
          );
        }
      });
  }

  removeDepartment(department: Department): void {
    if (
      !department.id ||
      !this.auth.isAdmin() ||
      this.busy()
    ) {
      return;
    }

    const confirmed = confirm(
      `Are you sure you want to delete ${department.name}?`
    );

    if (!confirmed) {
      return;
    }

    this.busy.set(true);

    this.api.deleteDepartment(department.id)
      .pipe(
        finalize(() => this.busy.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: () => {
          this.flash('success', 'Department deleted.');

          this.loadAll();
          this.loadEmployeePage();
        },
        error: error => {
          this.flash(
            'error',
            this.message(error, 'Unable to delete department.')
          );
        }
      });
  }

  // ---------------------------------------------------------------------------
  // Employee-to-Department integration demo
  // ---------------------------------------------------------------------------

  runLookup(): void {
    if (this.lookupForm.invalid || this.busy()) {
      this.lookupForm.markAllAsTouched();
      return;
    }

    this.lookupResult.set(null);
    this.busy.set(true);

    const departmentId =
      this.lookupForm.getRawValue().departmentId;

    this.api.departmentThroughEmployee(departmentId)
      .pipe(
        finalize(() => this.busy.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: department => {
          this.lookupResult.set(department);

          this.flash(
            'success',
            'RestClient integration flow completed successfully.'
          );
        },
        error: error => {
          this.flash(
            'error',
            this.message(error, 'Integration call failed.')
          );
        }
      });
  }

  // ---------------------------------------------------------------------------
  // Dashboard helpers
  // ---------------------------------------------------------------------------

  deptName(departmentId: number): string {
    return this.departments().find(
      department => department.id === departmentId
    )?.name ?? `Department #${departmentId}`;
  }

  donutGradient(): string {
    const colors = [
      '#255b46',
      '#8cbf9b',
      '#d6b36b',
      '#9c8ac8',
      '#d58b76',
      '#6e9fb4'
    ];

    const values = this.departments().map(
      department => this.employeeCount(department.id)
    );

    const total = values.reduce(
      (sum, value) => sum + value,
      0
    );

    if (total === 0) {
      return 'conic-gradient(#e8ece8 0deg 360deg)';
    }

    let start = 0;

    const segments = values.map((value, index) => {
      const end = start + (value / total) * 360;

      const segment =
        `${colors[index % colors.length]} ${start}deg ${end}deg`;

      start = end;

      return segment;
    });

    return `conic-gradient(${segments.join(', ')})`;
  }

  emptyDepartmentCount(): number {
    return this.departments().filter(
      department => this.employeeCount(department.id) === 0
    ).length;
  }

  activeDepartmentCount(): number {
    return this.departments().filter(
      department => this.employeeCount(department.id) > 0
    ).length;
  }

  employeeCount(departmentId?: number): number {
    return this.employees().filter(
      employee => employee.departmentId === departmentId
    ).length;
  }

  initials(name: string): string {
    return name
      .trim()
      .split(/\s+/)
      .filter(Boolean)
      .slice(0, 2)
      .map(part => part.charAt(0))
      .join('')
      .toUpperCase();
  }

  setView(view: View): void {
    this.view.set(view);
  }

  // ---------------------------------------------------------------------------
  // Notifications and error handling
  // ---------------------------------------------------------------------------

  private flash(
    type: 'success' | 'error',
    text: string
  ): void {
    this.notice.set({ type, text });

    if (this.noticeTimeout) {
      clearTimeout(this.noticeTimeout);
    }

    this.noticeTimeout = setTimeout(() => {
      this.notice.set(null);
      this.noticeTimeout = undefined;
    }, 4500);
  }

  private message(
    error: unknown,
    fallback: string
  ): string {
    if (!this.isRecord(error)) {
      return fallback;
    }

    const errorBody = error['error'];

    if (this.isRecord(errorBody)) {
      const message =
        errorBody['message'] ?? errorBody['error'];

      if (typeof message === 'string' && message.trim()) {
        return message;
      }
    }

    if (typeof errorBody === 'string' && errorBody.trim()) {
      return errorBody;
    }

    return fallback;
  }

  private isRecord(
    value: unknown
  ): value is Record<string, unknown> {
    return typeof value === 'object' && value !== null;
  }
}
