export declare type IStrToDate = (date: string) => Date;
export declare type IDateToStr = (date: Date) => string;
export declare type IDateAdd = (date: Date, inc: number, mode: string) => Date;
export declare class DateFormatter {
    locale: any;
    startOnMonday: boolean;
    private _len;
    private custom;
    constructor();
    date_part(date: Date): Date;
    time_part(date: Date): number;
    week_start(date: Date): Date;
    month_start(date: Date): Date;
    month_end(date: Date): Date;
    year_start(date: Date): Date;
    day_start(date: Date): Date;
    length(a: Date, b: Date, unit: string): number;
    _add_days(date: Date, inc: number): Date;
    registerCustomMode(name: string, handler: IDateAdd): void;
    add(date: Date, inc: number, mode: string): Date;
    to_fixed(num: number): string;
    copy(date: Date): Date;
    date_to_str(format: string, utc?: boolean): IDateToStr;
    str_to_date(format: string, utc?: boolean): IStrToDate;
    getISOWeek(ndate: Date): number;
    getUTCISOWeek(ndate: Date): number;
    convert_to_utc(date: Date): Date;
    getQuarter(d: Date): number;
}
