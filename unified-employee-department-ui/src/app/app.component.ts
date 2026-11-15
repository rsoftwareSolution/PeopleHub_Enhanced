import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize, forkJoin } from 'rxjs';
import { ApiService } from './core/api.service';
import { AuthService } from './core/auth.service';
import { Department, Employee } from './models/models';

type View = 'dashboard' | 'employees' | 'departments' | 'integration';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './app.component.html'
})
export class AppComponent implements OnInit {
  readonly view = signal<View>('dashboard');
  readonly employees = signal<Employee[]>([]);
  readonly departments = signal<Department[]>([]);
  readonly busy = signal(false);
  readonly loading = signal(false);
  readonly notice = signal<{type: 'success'|'error'; text: string} | null>(null);
  readonly showEmployeeModal = signal(false);
  readonly showDepartmentModal = signal(false);
  readonly editingEmployeeId = signal<number | null>(null);
  readonly editingDepartmentId = signal<number | null>(null);
  readonly lookupResult = signal<Department | null>(null);
  readonly employeeSearch = signal('');
  readonly departmentSearch = signal('');

  readonly filteredEmployees = computed(() => {
    const q = this.employeeSearch().toLowerCase();
    return this.employees().filter(e => `${e.name} ${e.email} ${e.departmentId}`.toLowerCase().includes(q));
  });
  readonly filteredDepartments = computed(() => {
    const q = this.departmentSearch().toLowerCase();
    return this.departments().filter(d => `${d.name} ${d.location}`.toLowerCase().includes(q));
  });

  loginForm = this.fb.nonNullable.group({
    username: ['admin', Validators.required],
    password: ['admin123', Validators.required]
  });
  employeeForm = this.fb.nonNullable.group({
    name: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    departmentId: [1, [Validators.required, Validators.min(1)]]
  });
  departmentForm = this.fb.nonNullable.group({
    name: ['', Validators.required],
    location: ['', Validators.required]
  });
  lookupForm = this.fb.nonNullable.group({
    departmentId: [1, [Validators.required, Validators.min(1)]]
  });

  constructor(public auth: AuthService, private api: ApiService, private fb: FormBuilder) {}

  ngOnInit(): void { if (this.auth.isLoggedIn()) this.loadAll(); }

  login(): void {
    if (this.loginForm.invalid) return;
    this.busy.set(true);
    const { username, password } = this.loginForm.getRawValue();
    this.auth.login(username, password).pipe(finalize(() => this.busy.set(false))).subscribe({
      next: () => { this.flash('success', `Signed in as ${this.auth.role()}`); this.loadAll(); },
      error: err => this.flash('error', this.message(err, 'Login failed. Check credentials and Employee Service.'))
    });
  }

  logout(): void { this.auth.logout(); this.employees.set([]); this.departments.set([]); this.view.set('dashboard'); }

  loadAll(): void {
    this.loading.set(true);
    forkJoin({ employees: this.api.employees(), departments: this.api.departments() })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: data => { this.employees.set(data.employees); this.departments.set(data.departments); },
        error: err => this.flash('error', this.message(err, 'Unable to load dashboard data. Confirm both services and CORS settings.'))
      });
  }

  openEmployee(employee?: Employee): void {
    this.editingEmployeeId.set(employee?.id ?? null);
    this.employeeForm.reset({ name: employee?.name ?? '', email: employee?.email ?? '', departmentId: employee?.departmentId ?? (this.departments()[0]?.id ?? 1) });
    this.showEmployeeModal.set(true);
  }
  saveEmployee(): void {
    if (this.employeeForm.invalid || !this.auth.isAdmin()) return;
    this.busy.set(true);
    const value = this.employeeForm.getRawValue();
    const id = this.editingEmployeeId();
    const request = id ? this.api.updateEmployee(id, value) : this.api.createEmployee(value);
    request.pipe(finalize(() => this.busy.set(false))).subscribe({
      next: () => { this.showEmployeeModal.set(false); this.flash('success', id ? 'Employee updated.' : 'Employee created.'); this.loadAll(); },
      error: err => this.flash('error', this.message(err, 'Employee operation failed.'))
    });
  }
  removeEmployee(employee: Employee): void {
    if (!employee.id || !this.auth.isAdmin() || !confirm(`Delete ${employee.name}?`)) return;
    this.api.deleteEmployee(employee.id).subscribe({ next: () => { this.flash('success', 'Employee deleted.'); this.loadAll(); }, error: err => this.flash('error', this.message(err, 'Delete failed.')) });
  }

  openDepartment(department?: Department): void {
    this.editingDepartmentId.set(department?.id ?? null);
    this.departmentForm.reset({ name: department?.name ?? '', location: department?.location ?? '' });
    this.showDepartmentModal.set(true);
  }
  saveDepartment(): void {
    if (this.departmentForm.invalid || !this.auth.isAdmin()) return;
    this.busy.set(true);
    const value = this.departmentForm.getRawValue();
    const id = this.editingDepartmentId();
    const request = id ? this.api.updateDepartment(id, value) : this.api.createDepartment(value);
    request.pipe(finalize(() => this.busy.set(false))).subscribe({
      next: () => { this.showDepartmentModal.set(false); this.flash('success', id ? 'Department updated.' : 'Department created.'); this.loadAll(); },
      error: err => this.flash('error', this.message(err, 'Department operation failed.'))
    });
  }
  removeDepartment(department: Department): void {
    if (!department.id || !this.auth.isAdmin() || !confirm(`Delete ${department.name}?`)) return;
    this.api.deleteDepartment(department.id).subscribe({ next: () => { this.flash('success', 'Department deleted.'); this.loadAll(); }, error: err => this.flash('error', this.message(err, 'Delete failed.')) });
  }

  runLookup(): void {
    if (this.lookupForm.invalid) return;
    this.lookupResult.set(null);
    this.busy.set(true);
    this.api.departmentThroughEmployee(this.lookupForm.getRawValue().departmentId)
      .pipe(finalize(() => this.busy.set(false)))
      .subscribe({ next: d => { this.lookupResult.set(d); this.flash('success', 'RestClient flow completed successfully.'); }, error: err => this.flash('error', this.message(err, 'Integration call failed.')) });
  }

  deptName(id: number): string { return this.departments().find(d => d.id === id)?.name ?? `Department #${id}`; }
  donutGradient(): string {
    const colors = ['#255b46','#8cbf9b','#d6b36b','#9c8ac8','#d58b76','#6e9fb4'];
    const values = this.departments().map(d => this.employeeCount(d.id));
    const total = values.reduce((sum, value) => sum + value, 0);
    if (!total) return 'conic-gradient(#e8ece8 0deg 360deg)';
    let start = 0;
    const segments = values.map((value, index) => {
      const end = start + value / total * 360;
      const segment = `${colors[index % colors.length]} ${start}deg ${end}deg`;
      start = end;
      return segment;
    });
    return `conic-gradient(${segments.join(',')})`;
  }
  emptyDepartmentCount(): number { return this.departments().filter(d => this.employeeCount(d.id) === 0).length; }
  activeDepartmentCount(): number { return this.departments().filter(d => this.employeeCount(d.id) > 0).length; }
  employeeCount(departmentId?: number): number { return this.employees().filter(e => e.departmentId === departmentId).length; }
  initials(name: string): string { return name.split(' ').map(p => p[0]).slice(0,2).join('').toUpperCase(); }
  setView(view: View): void { this.view.set(view); }
  private flash(type: 'success'|'error', text: string): void { this.notice.set({type, text}); setTimeout(() => this.notice.set(null), 4500); }
  private message(err: any, fallback: string): string { return err?.error?.message || err?.error?.error || (typeof err?.error === 'string' ? err.error : '') || fallback; }
}
