import React from 'react';
import {AuthProvider} from './contexts/AuthContext';
import {BrowserRouter as Router, Routes, Route} from 'react-router-dom';
import Login from './components/Login/Login';
import Register from './components/Register/Register';
import Home from './pages/Home/Home';
import PrivateRoute from './components/PrivateRoute/PrivateRoute';
import UserPanel from './components/UserPanel/UserPanel';
import AdminPanel from './components/AdminPanel/AdminPanel';
import AdminRoute from './components/AdminRoute';
import AddProduct from './components/AdminPanel/AddProduct';
import ProductList from './components/AdminPanel/ProductList';
import EditProduct from './components/AdminPanel/EditProduct';
import ProductDetail from './components/ProductDetail/ProductDetail';
import CategoryList from './components/AdminPanel/CategoryList';
import AddCategory from './components/AdminPanel/AddCategory';
import EditCategory from './components/AdminPanel/EditCategory';
import Cart from './components/Cart/Cart'
import UserOrders from './components/UserOrders/UserOrders';
import DiscountCodesAdmin from './components/AdminPanel/DiscountCodesAdmin';
import Navbar from "./components/Navbar/Navbar";
import AdminOrders from './components/AdminPanel/AdminOrder';
import Footer from "./components/Footer/Footer";


function App() {
    return (
        <AuthProvider>

            <Router>
                <Navbar/>
                <Routes>
                    {/* Publiczne trasy */}
                    <Route path="/login" element={<Login/>}/>
                    <Route path="/register" element={<Register/>}/>
                    <Route path="/" element={<Home/>}/>
                    <Route path="/products/:id" element={<ProductDetail/>}/>
                    <Route path="/cart" element={<Cart/>}/>
                    <Route path="/orders" element={<UserOrders/>}/>
                    {/* Trasy dla zalogowanych użytkowników */}
                    <Route
                        path="/user-panel"
                        element={
                            <PrivateRoute>
                                <UserPanel/>
                            </PrivateRoute>
                        }
                    />

                    {/* Trasy dla administratora */}
                    <Route
                        path="/admin"
                        element={
                            <AdminRoute>
                                <AdminPanel/>
                            </AdminRoute>
                        }
                    />
                    <Route path="/admin/discount-codes" element={<AdminRoute><DiscountCodesAdmin/></AdminRoute>}/>
                    <Route
                        path="/admin/add-product"
                        element={
                            <AdminRoute>
                                <AddProduct/>
                            </AdminRoute>
                        }
                    />
                    <Route
                        path="/admin/products"
                        element={
                            <AdminRoute>
                                <ProductList/>
                            </AdminRoute>
                        }
                    />
                    <Route
                        path="/admin/products/edit/:id"
                        element={
                            <AdminRoute>
                                <EditProduct/>
                            </AdminRoute>
                        }
                    />
                    <Route
                        path="/admin/categories"
                        element={
                            <AdminRoute>
                                <CategoryList/>
                            </AdminRoute>
                        }
                    />
                    <Route
                        path="/admin/add-category"
                        element={
                            <AdminRoute>
                                <AddCategory/>
                            </AdminRoute>
                        }
                    />
                    <Route
                        path="/admin/categories/edit/:id"
                        element={
                            <AdminRoute>
                                <EditCategory/>
                            </AdminRoute>
                        }
                    />
                    <Route path="/admin/orders" element={<AdminRoute><AdminOrders/></AdminRoute>}/>

                </Routes>
                <Footer/>
            </Router>
        </AuthProvider>
    );
}

export default App;
