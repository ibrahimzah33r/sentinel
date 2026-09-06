import type { ReactNode } from 'react'
import Sidebar from '../components/Sidebar'

type AppLayoutProps = {
  children: ReactNode
}

function AppLayout({ children }: AppLayoutProps) {
  return (
    <div className="app-layout">
      <Sidebar />

      <main className="app-content">
        {children}
      </main>
    </div>
  )
}

export default AppLayout