export default function Mensaje({ error, ok }) {
  if (!error && !ok) return null
  return <div className={'alert py-2 ' + (error ? 'alert-danger' : 'alert-success')}>{error || ok}</div>
}
