package com.xzie.user_service.service;

import com.xzie.user_service.dao.ProductRepo;
import com.xzie.user_service.dto.ChatProduct;
import com.xzie.user_service.dto.ChatTurn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private StoreKnowledge knowledge;

    // rebuilt on every message, so stock changes, edits and new products show up immediately.
    public String systemPrompt() {
        StringBuilder sb = new StringBuilder(knowledge.text());
        sb.append("\n\nCURRENT PRODUCTS (live from the database, the only product information you may use):\n");

        List<ChatProduct> list = productRepo.findForChat();
        if (list.isEmpty()) sb.append("(no products are listed right now)\n");
        for (ChatProduct p : list.stream().limit(100).toList()) {
            String d = p.description() == null ? "" : p.description().replaceAll("\\s+", " ");
            if (d.length() > 120) d = d.substring(0, 120) + "...";
            sb.append(String.format("[id %d] %s | %s | INR %s | stock: %d | %s%n",
                    p.id(), p.name(), p.category(), p.price(), p.stock(), d));
        }
        sb.append("\nFINAL REMINDER: Answer only about this store, its products and its policies, using only the product list above. For anything else, such as coding, general knowledge or other topics, reply in one sentence that you can only help with shopping at haul. Never invent products, prices, discounts or promo codes.\n");
        return sb.toString();
    }

    public List<ChatTurn> trim(List<ChatTurn> history) {   // keep only the last 6 messages
        if (history == null) return List.of();
        return history.size() <= 6 ? history : history.subList(history.size() - 6, history.size());
    }
}