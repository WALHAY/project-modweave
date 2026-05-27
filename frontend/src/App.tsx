import { Route, Routes } from 'react-router-dom'
import Layout from './components/Layout'
import Home from './pages/Home'
import Browse from './pages/Browse'
import BrowseGames from './pages/BrowseGames'
import ModDetail from './pages/ModDetail'
import ModUpload from './pages/ModUpload'
import FileDetail from './pages/FileDetail'
import VersionDetail from './pages/VersionDetail'
import Profile from './pages/Profile'
import Login from './pages/Login'
import NotFound from './pages/NotFound'
import AdminCategoryUpload from './pages/AdminCategoryUpload'
import AdminGameUpload from './pages/AdminGameUpload'

function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<Home />} />
        <Route path="mods" element={<Browse />} />
        <Route path="mods/upload" element={<ModUpload />} />
        <Route path="mods/:modId" element={<ModDetail />} />
        <Route path="mods/:modId/versions/:versionId" element={<VersionDetail />} />
        <Route path="games" element={<BrowseGames />} />
        <Route path="files/:bucket/:fileId" element={<FileDetail />} />
        <Route path="admin/categories" element={<AdminCategoryUpload />} />
        <Route path="admin/games" element={<AdminGameUpload />} />
        <Route path="users/:username" element={<Profile />} />
        <Route path="login" element={<Login />} />
      </Route>
      <Route path="*" element={<NotFound />} />
    </Routes>
  )
}

export default App
