document.addEventListener('DOMContentLoaded', () => {
    // --- Elementos del DOM ---
    const searchForm = document.getElementById('search-form');
    const productNameInput = document.getElementById('product-name-input');
    const resultsGrid = document.getElementById('results-grid');
    const loadingSpinner = document.getElementById('loading-spinner');
    const infoMessage = document.getElementById('info-message');
    const cardTemplate = document.getElementById('product-card-template');
    const resultsCount = document.getElementById('results-count');

    // --- Variables de Estado ---
    let currentProducts = [];
    let currentSort = 'price-asc';

    // --- Event Listeners ---
    searchForm.addEventListener('submit', async (event) => {
        event.preventDefault();
        await startSearch();
    });

    document.querySelectorAll('.sort-option').forEach(option => {
        option.addEventListener('click', (e) => {
            e.preventDefault();
            currentSort = e.target.dataset.sort;
            updateActiveSortButton();
            sortProducts(currentSort);
        });
    });

    // --- Funciones Principales ---
    const startSearch = async () => {
        const productName = productNameInput.value.trim();

        if (!productName) {
            showInfoMessage('Por favor, ingresa un nombre de producto.', 'warning');
            return;
        }

        prepareUIForSearch();

        try {
            console.log("Iniciando búsqueda de:", productName);
            const response = await fetch(`/api/products/search?name=${encodeURIComponent(productName)}`);

            if (!response.ok) {
                const errorText = await response.text();
                console.error("Error en la respuesta:", response.status, errorText);
                throw new Error(`Error al buscar productos (${response.status})`);
            }

            currentProducts = await response.json();
            console.log("Productos recibidos:", currentProducts);

            if (!Array.isArray(currentProducts)) {
                throw new Error("Formato de respuesta inválido");
            }

            renderResults(currentProducts);

        } catch (error) {
            console.error("Error en la búsqueda:", error);
            showInfoMessage(error.message, 'danger');
            currentProducts = [];
            renderResults([]);
        } finally {
            restoreUIAfterSearch();
        }
    };

    // --- Funciones de Soporte ---
    function prepareUIForSearch() {
        loadingSpinner.hidden = false;
        resultsGrid.innerHTML = '';
        infoMessage.hidden = true;
        searchForm.querySelector('button').disabled = true;
    }

    function restoreUIAfterSearch() {
        loadingSpinner.hidden = true;
        searchForm.querySelector('button').disabled = false;
    }

    function updateActiveSortButton() {
        document.querySelectorAll('.sort-option').forEach(option => {
            option.classList.toggle('active', option.dataset.sort === currentSort);
        });
    }

    function sortProducts(sortMethod) {
        if (!currentProducts.length) return;

        const sortedProducts = [...currentProducts].sort((a, b) => {
            try {
                const priceA = parsePrice(a.currentPrice);
                const priceB = parsePrice(b.currentPrice);

                switch (sortMethod) {
                    case 'price-asc': return priceA - priceB;
                    case 'price-desc': return priceB - priceA;
                    case 'rating':
                        return (b.rating || 0) - (a.rating || 0);
                    case 'store':
                        return (a.storeName || '').localeCompare(b.storeName || '');
                    default: return 0;
                }
            } catch (error) {
                console.error("Error al ordenar:", error);
                return 0;
            }
        });

        renderResults(sortedProducts);
    }

    function renderResults(products) {
        resultsGrid.innerHTML = '';

        if (!Array.isArray(products)) {
            showInfoMessage('Error al procesar los resultados', 'danger');
            return;
        }

        resultsCount.textContent = products.length > 0
            ? `${products.length} productos encontrados`
            : '';

        if (products.length === 0) {
            showInfoMessage('No se encontraron productos con ese nombre. Intenta con términos diferentes.', 'info');
            return;
        }

        products.forEach(product => {
            try {
                const card = createProductCard(product);
                if (card) resultsGrid.appendChild(card);
            } catch (error) {
                console.error("Error al crear tarjeta de producto:", product, error);
            }
        });
    }

    function createProductCard(product) {
        if (!product || typeof product !== 'object') {
            console.error("Producto inválido:", product);
            return null;
        }

        const card = cardTemplate.content.cloneNode(true).querySelector('.card');

        // Datos básicos con valores por defecto
        const img = card.querySelector('img');
        img.src = product.imageUrl || 'https://via.placeholder.com/300x200?text=Sin+imagen';
        img.alt = product.name || 'Producto sin nombre';

        card.querySelector('.card-title').textContent = product.name || 'Nombre no disponible';
        card.querySelector('.store-name').textContent = product.storeName || 'Tienda desconocida';

        const priceElement = card.querySelector('.card-price');
        priceElement.textContent = product.currentPrice !== undefined
            ? formatPrice(product.currentPrice)
            : 'Precio no disponible';

        const link = card.querySelector('a');
        if (product.productUrl) {
            link.href = product.productUrl;
        } else {
            link.style.pointerEvents = 'none';
            link.classList.add('text-muted');
        }

        // Disponibilidad
        const availability = card.querySelector('.availability-badge');
        if (product.available !== undefined) {
            availability.className = `badge bg-${product.available ? 'success' : 'danger'}`;
            availability.textContent = product.available ? 'Disponible' : 'Agotado';
        } else {
            availability.className = 'badge bg-secondary';
            availability.textContent = 'Estado desconocido';
        }

        // Rating
        const ratingContainer = card.querySelector('.rating-stars');
        if (product.rating !== undefined && product.rating !== null) {
            ratingContainer.innerHTML = generateRatingStars(product.rating);
        } else {
            ratingContainer.innerHTML = '<span class="text-muted">Sin valoraciones</span>';
        }

        return card;
    }

    // --- Funciones Utilitarias ---
    function parsePrice(price) {
        if (price === undefined || price === null) {
            return 0;
        }

        if (typeof price === 'number') {
            return price;
        }

        if (typeof price === 'string') {
            // Elimina todo excepto números y punto decimal
            const cleaned = price.replace(/[^\d.]/g, '');
            return parseFloat(cleaned) || 0;
        }

        return 0;
    }

    function formatPrice(price) {
        try {
            const numericPrice = parsePrice(price);
            return new Intl.NumberFormat('es-MX', {
                style: 'currency',
                currency: 'MXN'
            }).format(numericPrice);
        } catch (error) {
            console.error("Error al formatear precio:", price, error);
            return 'Precio no disponible';
        }
    }

    function generateRatingStars(rating) {
        const numericRating = Number(rating) || 0;
        const clampedRating = Math.min(Math.max(numericRating, 0), 5);
        const fullStars = Math.floor(clampedRating);
        const hasHalfStar = clampedRating % 1 >= 0.5;

        return Array.from({length: 5}, (_, i) => {
            if (i < fullStars) return '<i class="bi bi-star-fill"></i>';
            if (i === fullStars && hasHalfStar) return '<i class="bi bi-star-half"></i>';
            return '<i class="bi bi-star"></i>';
        }).join('');
    }

    function showInfoMessage(message, type) {
        infoMessage.hidden = false;
        infoMessage.className = `alert alert-${type} fade show`;
        infoMessage.innerHTML = `
            <div class="d-flex align-items-center">
                <i class="bi ${type === 'danger' ? 'bi-exclamation-triangle' : type === 'warning' ? 'bi-exclamation-circle' : 'bi-info-circle'} me-2"></i>
                <span>${message}</span>
            </div>
        `;
    }
});