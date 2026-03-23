/**
 * KS-Market Price Tracker — Frontend Logic
 */

// ===== Toast Notification System =====

function showToast(message, type) {
    const container = document.getElementById('toastContainer');
    const toastId = 'toast-' + Date.now();

    const toastHtml = `
        <div id="${toastId}" class="toast ${type === 'success' ? 'toast-success' : 'toast-error'}" 
             role="alert" aria-live="assertive" aria-atomic="true">
            <div class="d-flex align-items-center p-3">
                <i class="bi ${type === 'success' ? 'bi-check-circle-fill' : 'bi-x-circle-fill'} me-2 fs-5"></i>
                <div class="flex-grow-1 fw-medium">${message}</div>
                <button type="button" class="btn-close ms-2" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        </div>
    `;

    container.insertAdjacentHTML('beforeend', toastHtml);

    const toastEl = document.getElementById(toastId);
    const toast = new bootstrap.Toast(toastEl, { delay: 4000 });
    toast.show();

    toastEl.addEventListener('hidden.bs.toast', () => toastEl.remove());
}


// ===== Add Product Form =====

const addForm = document.getElementById('addProductForm');
if (addForm) {
    addForm.addEventListener('submit', async function (e) {
        e.preventDefault();

        const urlInput = document.getElementById('productUrl');
        const addBtn = document.getElementById('addBtn');
        const spinner = document.getElementById('addSpinner');
        const url = urlInput.value.trim();

        if (!url) {
            showToast('Будь ласка, введіть посилання', 'error');
            return;
        }

        // Show loading state
        addBtn.disabled = true;
        spinner.classList.remove('d-none');

        try {
            const response = await fetch('/api/products', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ url: url })
            });

            const data = await response.json();

            if (data.success) {
                showToast(data.message, 'success');
                urlInput.value = '';

                // Add new card to the grid
                addProductCard(data.product);

                // Remove empty state if present
                const emptyState = document.getElementById('emptyState');
                if (emptyState) emptyState.remove();
            } else {
                showToast(data.message, 'error');
            }
        } catch (err) {
            showToast('Не вдалося додати товар. Спробуйте пізніше.', 'error');
            console.error('Add product error:', err);
        } finally {
            addBtn.disabled = false;
            spinner.classList.add('d-none');
        }
    });
}


// ===== Add Product Card Dynamically =====

function addProductCard(product) {
    const grid = document.getElementById('productsGrid');
    if (!grid) return;

    const updatedAt = formatDate(product.updatedAt);
    const stockBadge = product.inStock
        ? '<span class="badge bg-success-subtle text-success"><i class="bi bi-circle-fill me-1 small"></i>В наявності</span>'
        : '<span class="badge bg-secondary-subtle text-secondary"><i class="bi bi-circle me-1 small"></i>Немає</span>';

    const cardHtml = `
        <div class="col-sm-6 col-md-4 col-lg-3" id="card-${product.id}">
            <div class="card product-card h-100 shadow-sm border-0">
                <div class="card-img-wrapper">
                    <img src="${product.imageUrl || '/css/no-image.svg'}" 
                         alt="${escapeHtml(product.name)}"
                         class="card-img-top product-img"
                         onerror="this.src='/css/no-image.svg'">
                </div>
                <div class="card-body d-flex flex-column">
                    <h6 class="card-title product-name">${escapeHtml(product.name)}</h6>
                    <div class="mt-auto">
                        <div class="d-flex align-items-center mb-2">
                            ${stockBadge}
                        </div>
                        <div class="text-muted small mb-1">
                            <i class="bi bi-calendar3 me-1"></i>${updatedAt}
                        </div>
                        <div class="price-block">
                            <span class="price-uah fw-bold">${formatPrice(product.priceUah)} ₴</span>
                            <span class="text-muted mx-1">/</span>
                            <span class="price-usd text-success">$${formatPrice(product.priceUsd)}</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `;

    grid.insertAdjacentHTML('beforeend', cardHtml);
}


// ===== Refresh Product =====

document.addEventListener('click', async function (e) {
    const refreshBtn = e.target.closest('.btn-refresh');
    if (!refreshBtn) return;

    const productId = refreshBtn.dataset.id;
    refreshBtn.disabled = true;
    refreshBtn.innerHTML = '<span class="spinner-border spinner-border-sm"></span>';

    try {
        const response = await fetch(`/api/products/${productId}/refresh`, { method: 'PUT' });
        const data = await response.json();

        if (data.success) {
            showToast(data.message, 'success');
            // Reload page to refresh the table
            setTimeout(() => location.reload(), 800);
        } else {
            showToast(data.message, 'error');
        }
    } catch (err) {
        showToast('Не вдалося оновити ціну', 'error');
        console.error('Refresh error:', err);
    } finally {
        refreshBtn.disabled = false;
        refreshBtn.innerHTML = '<i class="bi bi-arrow-clockwise"></i>';
    }
});


// ===== Delete Product =====

document.addEventListener('click', async function (e) {
    const deleteBtn = e.target.closest('.btn-delete');
    if (!deleteBtn) return;

    if (!confirm('Ви впевнені, що хочете видалити цей товар?')) return;

    const productId = deleteBtn.dataset.id;
    deleteBtn.disabled = true;

    try {
        const response = await fetch(`/api/products/${productId}`, { method: 'DELETE' });
        const data = await response.json();

        if (data.success) {
            showToast(data.message, 'success');

            // Remove card from index page
            const card = document.getElementById('card-' + productId);
            if (card) {
                card.style.transition = 'opacity 0.3s, transform 0.3s';
                card.style.opacity = '0';
                card.style.transform = 'scale(0.95)';
                setTimeout(() => card.remove(), 300);
            }

            // Remove row from products table
            const row = document.getElementById('row-' + productId);
            if (row) {
                row.style.transition = 'opacity 0.3s';
                row.style.opacity = '0';
                setTimeout(() => row.remove(), 300);
            }
        } else {
            showToast(data.message, 'error');
        }
    } catch (err) {
        showToast('Не вдалося видалити товар', 'error');
        console.error('Delete error:', err);
    } finally {
        deleteBtn.disabled = false;
    }
});


// ===== Utility Functions =====

function formatPrice(value) {
    if (value == null) return '0.00';
    return parseFloat(value).toFixed(2);
}

function formatDate(dateStr) {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    const day = String(d.getDate()).padStart(2, '0');
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const year = d.getFullYear();
    const hours = String(d.getHours()).padStart(2, '0');
    const minutes = String(d.getMinutes()).padStart(2, '0');
    return `${day}.${month}.${year} ${hours}:${minutes}`;
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
