export default function Avatar({ name, large = false }) {
  return <div className={large ? 'avatar avatar-lg' : 'avatar'}>{name ? name.slice(0, 1) : '?'}</div>;
}
