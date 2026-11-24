import { Component, EventEmitter, Input, Output } from '@angular/core';
import { StatusBadge } from '../status-badge/status-badge';

export type TableColumnType = 'text' | 'status' | 'level' | 'actions';

export interface TableColumn {
  key: string;
  label: string;
  type?: TableColumnType;
}

export interface TableActionEvent {
  action: 'edit' | 'delete' | 'view';
  row: any;
}

@Component({
  selector: 'app-data-table',
  imports: [StatusBadge],
  templateUrl: './data-table.html',
  styleUrl: './data-table.scss',
})
export class DataTable {
  @Input({ required: true }) columns: TableColumn[] = [];
  @Input({ required: true }) rows: any[] = [];
  @Input() emptyMessage = 'Aucune donnée disponible.';
  @Output() tableAction = new EventEmitter<TableActionEvent>();

  emit(action: TableActionEvent['action'], row: any): void {
    this.tableAction.emit({ action, row });
  }
}
