import React, { useContext, useState, useEffect } from "react";
import AppContext from "../Context/Context";
import axios from "axios";
import CheckoutPopup from "./CheckoutPopup";
import { Button } from "react-bootstrap";
import { toast } from "react-toastify";

const Cart = () => {
  const { cart, removeFromCart, clearCart } = useContext(AppContext);

  const [cartItems, setCartItems] = useState([]);
  const [totalPrice, setTotalPrice] = useState(0);
  const [showModal, setShowModal] = useState(false);

  const baseUrl = import.meta.env.VITE_BASE_URL || "http://localhost:8080";

  // Load cart
  useEffect(() => {
    if (cart.length > 0) {
      setCartItems(cart);
    } else {
      setCartItems([]);
    }
  }, [cart]);

  // Calculate total
  useEffect(() => {
    const total = cartItems.reduce(
      (acc, item) => acc + item.price * item.quantity,
      0
    );
    setTotalPrice(total);
  }, [cartItems]);

  // Increase quantity
  const handleIncreaseQuantity = (itemId) => {
    const updatedCart = cartItems.map((item) => {
      if (item.id === itemId) {
        if (item.quantity < item.stockQuantity) {
          return { ...item, quantity: item.quantity + 1 };
        } else {
          toast.info("Cannot add more than available stock");
        }
      }
      return item;
    });

    setCartItems(updatedCart);
  };

  // Decrease quantity
  const handleDecreaseQuantity = (itemId) => {
    const updatedCart = cartItems.map((item) =>
      item.id === itemId
        ? { ...item, quantity: Math.max(item.quantity - 1, 1) }
        : item
    );

    setCartItems(updatedCart);
  };

  // Remove item
  const handleRemoveFromCart = (itemId) => {
    removeFromCart(itemId);
    const updatedCart = cartItems.filter((item) => item.id !== itemId);
    setCartItems(updatedCart);
  };

  // Convert image
  const convertBase64ToDataURL = (base64String, mimeType = "image/jpeg") => {
    const fallbackImage = "/fallback-image.jpg";

    if (!base64String) return fallbackImage;

    if (base64String.startsWith("data:")) return base64String;

    if (base64String.startsWith("http")) return base64String;

    return `data:${mimeType};base64,${base64String}`;
  };

  // Checkout
  const handleCheckout = async () => {
    try {
      for (const item of cartItems) {
        const updatedStock = item.stockQuantity - item.quantity;

        const updatedProduct = {
          ...item,
          stockQuantity: updatedStock,
        };

        const formData = new FormData();

        formData.append(
          "product",
          new Blob([JSON.stringify(updatedProduct)], {
            type: "application/json",
          })
        );

        await axios.put(`${baseUrl}/api/product/${item.id}`, formData, {
          headers: {
            "Content-Type": "multipart/form-data",
          },
        });
      }

      toast.success("Order placed successfully");

      clearCart();
      setCartItems([]);
      setShowModal(false);
    } catch (error) {
      console.error("Checkout error:", error);
      toast.error("Checkout failed");
    }
  };

  return (
    <div className="container mt-5 pt-5">
      <div className="row justify-content-center">
        <div className="col-md-10">
          <div className="card shadow">
            <div className="card-header bg-white">
              <h4 className="mb-0">Shopping Cart</h4>
            </div>

            <div className="card-body">
              {cartItems.length === 0 ? (
                <div className="text-center py-5">
                  <h5>Your cart is empty</h5>
                  <a href="/" className="btn btn-primary mt-3">
                    Continue Shopping
                  </a>
                </div>
              ) : (
                <>
                  <div className="table-responsive">
                    <table className="table table-hover align-middle">
                      <thead>
                        <tr>
                          <th>Product</th>
                          <th>Price</th>
                          <th>Quantity</th>
                          <th>Total</th>
                          <th>Action</th>
                        </tr>
                      </thead>

                      <tbody>
                        {cartItems.map((item) => (
                          <tr key={item.id}>
                            <td>
                              <div className="d-flex align-items-center">
                                <img
                                  src={convertBase64ToDataURL(item.imageData)}
                                  alt={item.name}
                                  width="80"
                                  height="80"
                                  className="rounded me-3"
                                  style={{ objectFit: "cover" }}
                                />

                                <div>
                                  <h6 className="mb-0">{item.name}</h6>
                                  <small className="text-muted">
                                    {item.brand}
                                  </small>
                                </div>
                              </div>
                            </td>

                            <td>₹ {item.price}</td>

                            <td>
                              <div
                                className="input-group input-group-sm"
                                style={{ width: "120px" }}
                              >
                                <button
                                  className="btn btn-outline-secondary"
                                  onClick={() =>
                                    handleDecreaseQuantity(item.id)
                                  }
                                >
                                  -
                                </button>

                                <input
                                  className="form-control text-center"
                                  value={item.quantity}
                                  readOnly
                                />

                                <button
                                  className="btn btn-outline-secondary"
                                  onClick={() =>
                                    handleIncreaseQuantity(item.id)
                                  }
                                >
                                  +
                                </button>
                              </div>
                            </td>

                            <td className="fw-bold">
                              ₹ {(item.price * item.quantity).toFixed(2)}
                            </td>

                            <td>
                              <button
                                className="btn btn-sm btn-outline-danger"
                                onClick={() =>
                                  handleRemoveFromCart(item.id)
                                }
                              >
                                Remove
                              </button>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>

                  <div className="card mt-3">
                    <div className="card-body d-flex justify-content-between">
                      <h5>Total:</h5>
                      <h5>₹ {totalPrice.toFixed(2)}</h5>
                    </div>
                  </div>

                  <div className="d-grid mt-4">
                    <Button
                      variant="primary"
                      size="lg"
                      onClick={() => setShowModal(true)}
                    >
                      Proceed to Checkout
                    </Button>
                  </div>
                </>
              )}
            </div>
          </div>
        </div>
      </div>

      <CheckoutPopup
        show={showModal}
        handleClose={() => setShowModal(false)}
        cartItems={cartItems}
        totalPrice={totalPrice}
        handleCheckout={handleCheckout}
      />
    </div>
  );
};

export default Cart;