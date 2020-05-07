
const AAAAFormatter= new Intl.DateTimeFormat('en-US', { year: 'numeric' });
const MMFormatter= new Intl.DateTimeFormat('en-US', { month: '2-digit' });
const DDFormatter= new Intl.DateTimeFormat('en-US', { day: '2-digit' });

const millisToDays = 1 /(24*60*60*1000)

/**
 * Output date as AAAA-MM-dd
 * @param d date to parsed, default value is "new Date()"
 */
export function parseDate(date: Date | number = new Date(), separator:"/" | "-" = "-") : string {
    return `${AAAAFormatter.format(date)}${separator}${MMFormatter.format(date)}${separator}${DDFormatter.format(date)}`
}


export function daysBetween(d1: Date, d2 : Date) : number {
    return (truncateToDays(d2).getTime() - truncateToDays(d1).getTime()) * millisToDays
}


export function truncateToDays(date: Date = new Date()) : Date {
    var d = new Date(date)
    d.setHours(0,0,0,0)
    return d;
}
