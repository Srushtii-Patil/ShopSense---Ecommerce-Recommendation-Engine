// ShopSense - Client State & Controller
let state = {
    activeUserId: 1,
    users: [],
    currentUser: null,
    recommendations: [],
    cart: { items: [], total: 0, remainingBudget: 0, userBudget: 0 },
    suggestions: [],
    activeFilter: 'ALL',
    activeSort: 'score'
};

// Initialize Application
document.addEventListener('DOMContentLoaded', async () => {
    setupEventListeners();
    await loadDatabaseStatus();
    await loadUsers();
    await refreshAll();
});

function setupEventListeners() {
    // User Switcher
    const userSelector = document.getElementById('userSelector');
    userSelector.addEventListener('change', async (e) => {
        state.activeUserId = parseInt(e.target.value);
        await refreshAll();
        showToast(`Switched customer profile to ${state.currentUser ? state.currentUser.name : 'User'}`);
    });

    // Budget Slider
    const slider = document.getElementById('budgetSlider');
    const sliderTag = document.getElementById('sliderValueTag');
    slider.addEventListener('input', (e) => {
        const val = parseFloat(e.target.value);
        sliderTag.textContent = `$${val.toLocaleString()}`;
        updateBudgetDisplays(val);
    });

    slider.addEventListener('change', async (e) => {
        const val = parseFloat(e.target.value);
        try {
            await fetch('/api/user/budget', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: `userId=${state.activeUserId}&budget=${val}`
            });
            await refreshAll();
            showToast(`Budget updated to $${val.toLocaleString()}`);
        } catch (err) {
            console.error('Failed to update budget:', err);
        }
    });

    // Filter Chips
    const chips = document.querySelectorAll('.filter-chip');
    chips.forEach(chip => {
        chip.addEventListener('click', () => {
            chips.forEach(c => c.classList.remove('active'));
            chip.classList.add('active');
            state.activeFilter = chip.getAttribute('data-category');
            renderRecommendations();
        });
    });

    // Sort Dropdown
    const sortSelect = document.getElementById('sortSelect');
    sortSelect.addEventListener('change', (e) => {
        state.activeSort = e.target.value;
        renderRecommendations();
    });

    // Clear Cart
    document.getElementById('clearCartBtn').addEventListener('click', async () => {
        await fetch('/api/cart/clear', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: `userId=${state.activeUserId}`
        });
        await loadCart();
        await loadCartSuggestions();
        renderRecommendations();
        showToast('Cart cleared');
    });

    // Checkout
    document.getElementById('checkoutBtn').addEventListener('click', handleCheckout);

    // Modals
    document.getElementById('viewHistoryBtn').addEventListener('click', openHistoryModal);
    document.getElementById('editPrefsBtn').addEventListener('click', openPrefsModal);
    document.getElementById('dbBadge').addEventListener('click', openDbModal);
    document.getElementById('prefsForm').addEventListener('submit', handleSavePreferences);

    // Close modals when clicking outside box
    document.querySelectorAll('.modal-overlay').forEach(modal => {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) modal.classList.remove('active');
        });
    });
}

// ==================== DATA FETCHING ====================

async function refreshAll() {
    await loadUserProfile();
    await loadCart();
    await loadRecommendations();
    await loadCartSuggestions();
}

async function loadDatabaseStatus() {
    try {
        const res = await fetch('/api/db-status');
        const data = await res.json();
        document.getElementById('dbModeText').textContent = data.mode || 'Active';
        document.getElementById('modalDbMode').textContent = data.mode || 'Active';
        document.getElementById('modalDbUrl').textContent = data.url || 'Connected';
        document.getElementById('modalDbPort').textContent = data.serverPort || '8080';
    } catch (e) {
        document.getElementById('dbModeText').textContent = 'Embedded DB';
    }
}

async function loadUsers() {
    try {
        const res = await fetch('/api/users');
        state.users = await res.json();
        const selector = document.getElementById('userSelector');
        selector.innerHTML = '';
        state.users.forEach(u => {
            const opt = document.createElement('option');
            opt.value = u.userId;
            opt.textContent = `${u.name} ($${u.budget.toLocaleString()} • ${u.priceSensitivity})`;
            if (u.userId === state.activeUserId) opt.selected = true;
            selector.appendChild(opt);
        });
    } catch (e) {
        console.error('Failed to load users:', e);
    }
}

async function loadUserProfile() {
    try {
        const res = await fetch(`/api/user?id=${state.activeUserId}`);
        state.currentUser = await res.json();
        renderUserProfile();
    } catch (e) {
        console.error('Failed to load user profile:', e);
    }
}

async function loadRecommendations() {
    try {
        const res = await fetch(`/api/recommendations?userId=${state.activeUserId}&limit=12`);
        state.recommendations = await res.json();
        renderRecommendations();
    } catch (e) {
        console.error('Failed to load recommendations:', e);
    }
}

async function loadCart() {
    try {
        const res = await fetch(`/api/cart?userId=${state.activeUserId}`);
        state.cart = await res.json();
        renderCart();
    } catch (e) {
        console.error('Failed to load cart:', e);
    }
}

async function loadCartSuggestions() {
    try {
        const res = await fetch(`/api/cart/suggestions?userId=${state.activeUserId}`);
        state.suggestions = await res.json();
        renderCartSuggestions();
    } catch (e) {
        console.error('Failed to load cart suggestions:', e);
    }
}

// ==================== RENDERING ====================

function renderUserProfile() {
    if (!state.currentUser) return;
    const u = state.currentUser;

    // Avatar initials
    const parts = u.name.split(' ');
    const initials = parts.map(p => p[0]).join('').substring(0, 2);
    document.getElementById('userAvatar').textContent = initials;
    document.getElementById('userName').textContent = u.name;
    document.getElementById('userEmail').textContent = u.email;

    // Tier badge
    const tierBadge = document.getElementById('userTierBadge');
    tierBadge.textContent = u.priceSensitivity;
    tierBadge.className = `tier-badge tier-${u.priceSensitivity}`;

    // Tags
    const tagsContainer = document.getElementById('userPrefsTags');
    tagsContainer.innerHTML = '';
    u.preferredCategories.forEach(cat => {
        const t = document.createElement('span');
        t.className = 'pref-tag';
        t.textContent = `📁 ${cat}`;
        tagsContainer.appendChild(t);
    });
    u.preferredBrands.forEach(b => {
        const t = document.createElement('span');
        t.className = 'pref-tag';
        t.textContent = `🏷️ ${b}`;
        tagsContainer.appendChild(t);
    });

    // Slider
    const slider = document.getElementById('budgetSlider');
    slider.value = u.budget;
    document.getElementById('sliderValueTag').textContent = `$${Math.round(u.budget).toLocaleString()}`;

    updateBudgetDisplays(u.budget);
}

function updateBudgetDisplays(totalBudget) {
    const cartSpent = state.cart ? state.cart.total : 0.0;
    const remaining = Math.max(0, totalBudget - cartSpent);

    document.getElementById('budgetAllocatedDisplay').textContent = `$${totalBudget.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
    document.getElementById('budgetRemainingDisplay').textContent = `$${remaining.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} left`;

    const spentPercent = totalBudget > 0 ? Math.min(100, (cartSpent / totalBudget) * 100) : 0;
    const remainingPercent = Math.max(0, 100 - spentPercent);

    document.getElementById('progressSpent').style.width = `${spentPercent}%`;
    document.getElementById('progressRemaining').style.width = `${remainingPercent}%`;

    // Also update cart remaining budget field
    const cartRem = document.getElementById('cartRemainingBudgetVal');
    if (cartRem) {
        cartRem.textContent = `$${remaining.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
        cartRem.style.color = remaining >= 0 ? '#34d399' : '#f87171';
    }
}

function renderRecommendations() {
    const container = document.getElementById('recommendationsGrid');
    container.innerHTML = '';

    let items = [...state.recommendations];

    // Filter by Category
    if (state.activeFilter !== 'ALL') {
        items = items.filter(r => r.product.category.toLowerCase() === state.activeFilter.toLowerCase());
    }

    // Sort
    if (state.activeSort === 'price-asc') {
        items.sort((a, b) => a.product.price - b.product.price);
    } else if (state.activeSort === 'price-desc') {
        items.sort((a, b) => b.product.price - a.product.price);
    } else if (state.activeSort === 'rating') {
        items.sort((a, b) => b.product.rating - a.product.rating);
    } else {
        // default match score
        items.sort((a, b) => b.totalScore - a.totalScore);
    }

    if (items.length === 0) {
        container.innerHTML = `
            <div style="grid-column: 1 / -1; text-align: center; padding: 3rem; background: var(--bg-card); border-radius: var(--radius-lg); border: 1px dashed var(--border-color);">
                <p style="font-size: 1.2rem; color: var(--text-muted); margin-bottom: 0.5rem;">No recommendations matching category "${state.activeFilter}"</p>
                <p style="font-size: 0.85rem; color: var(--text-muted);">Try selecting "All Items" or adjusting your budget slider.</p>
            </div>
        `;
        return;
    }

    const remainingBudget = state.currentUser ? Math.max(0, state.currentUser.budget - state.cart.total) : 0;

    items.forEach(rec => {
        const p = rec.product;
        const fitsRemaining = p.price <= remainingBudget;

        const card = document.createElement('article');
        card.className = 'product-card';

        // Match reasons list
        const reasonsHtml = rec.matchReasons.map(reason => 
            `<li><span class="why-bullet">✓</span> ${escapeHtml(reason)}</li>`
        ).join('');

        card.innerHTML = `
            <div class="product-img-wrap">
                <img src="${escapeHtml(p.imageUrl)}" alt="${escapeHtml(p.name)}" class="product-img" loading="lazy" onerror="this.src='https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=500&q=80'">
                <div class="match-badge">
                    <span class="match-icon">⚡</span> ${Math.round(rec.totalScore)}% Match
                </div>
                <div class="category-tag">${escapeHtml(p.category)}</div>
            </div>
            <div class="product-body">
                <span class="brand-label">${escapeHtml(p.brand)}</span>
                <h4 class="product-name" title="${escapeHtml(p.name)}">${escapeHtml(p.name)}</h4>
                
                <div class="rating-row">
                    <span class="stars">★ ${p.rating.toFixed(1)}</span>
                    <span>(${p.reviewCount.toLocaleString()} reviews)</span>
                </div>

                <div class="why-recommended-box">
                    <div class="why-header">
                        <span>💡</span> Why Recommended
                    </div>
                    <ul class="why-list">
                        ${reasonsHtml}
                    </ul>
                </div>

                <div class="product-footer">
                    <div class="price-wrap">
                        <span class="price-val">$${p.price.toFixed(2)}</span>
                        <span class="budget-fit-note ${fitsRemaining ? 'fit-positive' : 'fit-negative'}">
                            ${fitsRemaining ? '✓ Within remaining budget' : `+$${(p.price - remainingBudget).toFixed(2)} over budget`}
                        </span>
                    </div>
                    <button class="add-cart-btn" onclick="addToCart(${p.productId}, '${escapeHtml(p.name.replace(/'/g, "\\'"))}')">
                        <span>+</span> Add to Cart
                    </button>
                </div>
            </div>
        `;
        container.appendChild(card);
    });
}

function renderCart() {
    const list = document.getElementById('cartItemsList');
    list.innerHTML = '';

    const items = state.cart.items || [];
    let totalItemsCount = 0;

    if (items.length === 0) {
        list.innerHTML = `<div class="empty-cart-msg">Your shopping cart is empty.<br><span style="font-size: 0.8rem; color: var(--text-muted);">Add recommended items to see dynamic suggestions.</span></div>`;
    } else {
        items.forEach(item => {
            totalItemsCount += item.quantity;
            const p = item.product || { name: 'Item', price: item.unitPrice, imageUrl: '' };
            const div = document.createElement('div');
            div.className = 'cart-item-card';
            div.innerHTML = `
                <img src="${escapeHtml(p.imageUrl)}" alt="${escapeHtml(p.name)}" class="cart-item-thumb" onerror="this.src='https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=100&q=80'">
                <div class="cart-item-details">
                    <div class="cart-item-name">${escapeHtml(p.name)}</div>
                    <div class="cart-item-sub">$${p.price.toFixed(2)} x ${item.quantity} = $${item.subtotal.toFixed(2)}</div>
                </div>
                <div class="cart-qty-ctrls">
                    <button class="qty-btn" onclick="updateQty(${item.cartItemId}, ${item.quantity - 1})" title="Decrease">-</button>
                    <span style="font-size: 0.8rem; font-weight: 700; min-width: 14px; text-align: center;">${item.quantity}</span>
                    <button class="qty-btn" onclick="updateQty(${item.cartItemId}, ${item.quantity + 1})" title="Increase">+</button>
                    <button class="qty-btn" style="background: rgba(239, 68, 68, 0.2); color: #f87171; margin-left: 2px;" onclick="removeItem(${item.cartItemId})" title="Remove">&times;</button>
                </div>
            `;
            list.appendChild(div);
        });
    }

    document.getElementById('cartCountBadge').textContent = totalItemsCount;

    // Subtotal and Totals
    const subtotal = state.cart.total || 0;
    const tax = subtotal * 0.08;
    const totalWithTax = subtotal + tax;

    document.getElementById('cartSubtotalVal').textContent = `$${subtotal.toFixed(2)}`;
    document.getElementById('cartTaxVal').textContent = `$${tax.toFixed(2)}`;
    document.getElementById('cartTotalVal').textContent = `$${totalWithTax.toFixed(2)}`;

    // Free Shipping Progress (Threshold: $100)
    const threshold = 100.00;
    const shippingBar = document.getElementById('shippingBarFill');
    const shippingText = document.getElementById('shippingGoalText');
    const shippingPercent = document.getElementById('shippingPercentText');

    if (subtotal >= threshold) {
        shippingBar.style.width = '100%';
        shippingBar.style.background = '#10b981';
        shippingText.innerHTML = '🎉 <strong>FREE EXPRESS SHIPPING UNLOCKED!</strong>';
        shippingPercent.textContent = '100%';
    } else {
        const gap = threshold - subtotal;
        const pct = Math.round((subtotal / threshold) * 100);
        shippingBar.style.width = `${pct}%`;
        shippingBar.style.background = 'var(--accent-gradient)';
        shippingText.textContent = `Add $${gap.toFixed(2)} more for FREE Delivery!`;
        shippingPercent.textContent = `${pct}%`;
    }

    if (state.currentUser) {
        updateBudgetDisplays(state.currentUser.budget);
    }
}

function renderCartSuggestions() {
    const list = document.getElementById('suggestionsList');
    list.innerHTML = '';

    const items = state.suggestions || [];
    if (items.length === 0) {
        list.innerHTML = `<div style="text-align: center; font-size: 0.8rem; color: var(--text-muted); padding: 1rem;">No suggestions available for current cart state.</div>`;
        return;
    }

    items.slice(0, 4).forEach(cs => {
        const p = cs.product;
        const div = document.createElement('div');
        div.className = 'sugg-card';

        div.innerHTML = `
            <div class="sugg-card-top">
                <img src="${escapeHtml(p.imageUrl)}" alt="${escapeHtml(p.name)}" class="sugg-thumb" onerror="this.src='https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=100&q=80'">
                <div class="sugg-info">
                    <span class="sugg-type-tag">${escapeHtml(cs.suggestionType.replace('_', ' '))}</span>
                    <div class="sugg-name" title="${escapeHtml(p.name)}">${escapeHtml(p.name)}</div>
                    <div class="sugg-price">$${p.price.toFixed(2)}</div>
                </div>
            </div>
            <p class="sugg-reason">${escapeHtml(cs.reason)}</p>
            <button class="sugg-add-btn" onclick="addToCart(${p.productId}, '${escapeHtml(p.name.replace(/'/g, "\\'"))}')">
                + Add Suggestion to Cart
            </button>
        `;
        list.appendChild(div);
    });
}

// ==================== ACTIONS ====================

async function addToCart(productId, productName) {
    try {
        await fetch('/api/cart/add', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: `userId=${state.activeUserId}&productId=${productId}&quantity=1`
        });
        showToast(`Added "${productName}" to cart! 🛒`);
        await loadCart();
        await loadCartSuggestions();
        renderRecommendations(); // re-evaluates remaining budget fit
    } catch (e) {
        console.error('Error adding to cart:', e);
    }
}

async function updateQty(cartItemId, newQty) {
    try {
        await fetch('/api/cart/update', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: `cartItemId=${cartItemId}&quantity=${newQty}`
        });
        await loadCart();
        await loadCartSuggestions();
        renderRecommendations();
    } catch (e) {
        console.error('Error updating quantity:', e);
    }
}

async function removeItem(cartItemId) {
    try {
        await fetch('/api/cart/remove', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: `cartItemId=${cartItemId}`
        });
        showToast('Item removed from cart');
        await loadCart();
        await loadCartSuggestions();
        renderRecommendations();
    } catch (e) {
        console.error('Error removing item:', e);
    }
}

async function handleCheckout() {
    if (!state.cart.items || state.cart.items.length === 0) {
        showToast('Your cart is empty! Add items first.');
        return;
    }

    try {
        const res = await fetch('/api/checkout', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: `userId=${state.activeUserId}`
        });
        const data = await res.json();
        if (data.success) {
            showToast(`🎉 Order #${data.orderId} placed for $${data.total.toFixed(2)}! AI model updated.`);
            await loadCart();
            await loadCartSuggestions();
            await loadRecommendations(); // Fresh collaborative filtering recommendations!
        } else {
            showToast(data.error || 'Checkout failed');
        }
    } catch (e) {
        console.error('Checkout error:', e);
    }
}

// ==================== MODAL HANDLERS ====================

async function openHistoryModal() {
    const modal = document.getElementById('historyModal');
    const timeline = document.getElementById('historyTimeline');
    timeline.innerHTML = '<div style="color: var(--text-muted); text-align: center;">Loading purchase history...</div>';
    modal.classList.add('active');

    try {
        const res = await fetch(`/api/orders?userId=${state.activeUserId}`);
        const orders = await res.json();

        if (orders.length === 0) {
            timeline.innerHTML = '<div style="text-align: center; color: var(--text-muted); padding: 1.5rem;">No prior orders on record for this customer profile.</div>';
            return;
        }

        timeline.innerHTML = '';
        orders.forEach(o => {
            const card = document.createElement('div');
            card.className = 'history-card';
            const itemsHtml = o.items.map(i => {
                const p = i.product || { name: 'Product #' + i.productId, category: '' };
                return `
                    <div class="history-item-row">
                        <span><strong>${escapeHtml(p.name)}</strong> (${escapeHtml(p.category)})</span>
                        <span>${i.quantity}x @ $${i.unitPrice.toFixed(2)}</span>
                    </div>
                `;
            }).join('');

            card.innerHTML = `
                <div class="history-card-header">
                    <span>Order #${o.orderId} • <span style="color: #34d399;">${escapeHtml(o.status)}</span></span>
                    <span>Total: $${o.totalAmount.toFixed(2)}</span>
                </div>
                <div style="font-size: 0.72rem; color: var(--text-muted); margin-bottom: 0.5rem;">
                    Placed on: ${o.orderDate ? o.orderDate.split(' ')[0] : 'Recent'}
                </div>
                <div>${itemsHtml}</div>
            `;
            timeline.appendChild(card);
        });
    } catch (e) {
        timeline.innerHTML = '<div style="color: #f87171;">Failed to load order history.</div>';
    }
}

function openPrefsModal() {
    if (!state.currentUser) return;
    const u = state.currentUser;
    document.getElementById('prefBudgetInput').value = u.budget;
    document.getElementById('prefCategoriesInput').value = u.preferredCategoriesRaw;
    document.getElementById('prefBrandsInput').value = u.preferredBrandsRaw;
    document.getElementById('prefTierSelect').value = u.priceSensitivity;
    document.getElementById('prefsModal').classList.add('active');
}

async function handleSavePreferences(e) {
    e.preventDefault();
    const budget = document.getElementById('prefBudgetInput').value;
    const categories = document.getElementById('prefCategoriesInput').value;
    const brands = document.getElementById('prefBrandsInput').value;
    const tier = document.getElementById('prefTierSelect').value;

    try {
        await fetch('/api/user/budget', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: `userId=${state.activeUserId}&budget=${budget}`
        });

        await fetch('/api/user/preferences', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: `userId=${state.activeUserId}&categories=${encodeURIComponent(categories)}&brands=${encodeURIComponent(brands)}&priceSensitivity=${encodeURIComponent(tier)}`
        });

        closeModal('prefsModal');
        showToast('Preferences updated successfully! Re-ranking recommendations...');
        await refreshAll();
    } catch (err) {
        console.error('Failed to save preferences:', err);
    }
}

function openDbModal() {
    document.getElementById('dbModal').classList.add('active');
}

function closeModal(id) {
    document.getElementById(id).classList.remove('active');
}

// ==================== TOAST UTILITY ====================

function showToast(msg) {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.innerHTML = `<span>⚡</span> <span>${escapeHtml(msg)}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(10px)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 3200);
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}
