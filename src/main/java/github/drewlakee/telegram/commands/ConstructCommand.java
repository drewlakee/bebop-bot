package github.drewlakee.telegram.commands;

import github.drewlakee.telegram.commands.callbacks.ConstructCallback;
import github.drewlakee.telegram.commands.callbacks.GlobalCallback;
import github.drewlakee.telegram.commands.handlers.BotCommand;
import github.drewlakee.telegram.commands.handlers.CallbackQueryHandler;
import github.drewlakee.telegram.commands.handlers.MessageHandler;
import github.drewlakee.telegram.commands.keyboards.HostGroupKeyboard;
import github.drewlakee.telegram.commands.keyboards.InlineKeyboardBuilder;
import github.drewlakee.telegram.commands.keyboards.NumpadKeyboardBuilder;
import github.drewlakee.telegram.utils.MessageKeysParser;
import github.drewlakee.telegram.utils.ResponseMessageDispatcher;
import github.drewlakee.vk.domain.groups.VkCustomGroup;
import github.drewlakee.vk.domain.groups.VkGroupObjective;
import github.drewlakee.vk.domain.vkObjects.VkAttachment;
import github.drewlakee.vk.domain.vkObjects.VkCustomAudio;
import github.drewlakee.vk.domain.vkObjects.VkCustomPhoto;
import github.drewlakee.vk.services.VkContentStrategyService;
import github.drewlakee.vk.services.VkWallPostService;
import github.drewlakee.vk.services.random.VkRandomAudioContent;
import github.drewlakee.vk.services.random.VkRandomPhotoContent;
import github.drewlakee.vk.singletons.VkGroupPool;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConstructCommand extends BotCommand implements CallbackQueryHandler, MessageHandler {

    public static final String COMMAND_NAME = "/construct";
    public static final int MAX_VK_ATTACHMENTS = 10;

    public ConstructCommand() {
        super(COMMAND_NAME);
    }

    @Override
    public void handle(AbsSender sender, Message message) {
        SendMessage response = new SendMessage();
        response.setChatId(message.getChatId());
        response.setText("Choose quantity of photos: ");
        NumpadKeyboardBuilder numpad = new NumpadKeyboardBuilder(4, MAX_VK_ATTACHMENTS);
        response.setReplyMarkup(numpad.build( COMMAND_NAME + "_first_call_photo", true));
        ResponseMessageDispatcher.send(sender, response);
    }

    @Override
    public void handle(AbsSender sender, CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        ConstructCallback handleCallback = ConstructCallback.CONSTRUCT_CALLBACK;
        int audiosQuantity = 0;
        int photosQuantity = 0;

        if (data.contains("photo_numpad")) {
            if (data.contains("_first_call_")) {
                photosQuantity = Integer.parseInt(data.replace(COMMAND_NAME + "_first_call_photo_numpad", ""));
                if (photosQuantity < 10) {
                    handleCallback = ConstructCallback.CHANGE_AUDIO_QUANTITY_CALLBACK;
                }
            } else {
                Map<String, String> keys = MessageKeysParser.parseMessageKeysBody(callbackQuery.getMessage().getText());
                photosQuantity = Integer.parseInt(data.replace(COMMAND_NAME + "photo_numpad", ""));
                audiosQuantity = Integer.parseInt(keys.get("audios_quantity"));
            }
        }

        if (data.contains("audio_numpad")) {
            Map<String, String> keys = MessageKeysParser.parseMessageKeysBody(callbackQuery.getMessage().getText());
            photosQuantity = Integer.parseInt(keys.get("photos_quantity"));
            audiosQuantity = Integer.parseInt(data.replace(COMMAND_NAME + "audio_numpad", ""));
        }

        if (data.contains(ConstructCallback.CHANGE_SET_CALLBACK.toCallbackString(COMMAND_NAME))) {
            Map<String, String> keys = MessageKeysParser.parseMessageKeysBody(callbackQuery.getMessage().getText());
            photosQuantity = Integer.parseInt(keys.get("photos_quantity"));
            audiosQuantity = Integer.parseInt(keys.get("audios_quantity"));
        }

        if (data.contains(ConstructCallback.CHANGE_AUDIO_QUANTITY_CALLBACK.toCallbackString(COMMAND_NAME))) {
            handleCallback = ConstructCallback.CHANGE_AUDIO_QUANTITY_CALLBACK;
            Map<String, String> keys = MessageKeysParser.parseMessageKeysBody(callbackQuery.getMessage().getText());
            photosQuantity = Integer.parseInt(keys.get("photos_quantity"));
            audiosQuantity = Integer.parseInt(keys.get("audios_quantity"));
        }

        if (data.contains(ConstructCallback.CHANGE_PHOTO_QUANTITY_CALLBACK.toCallbackString(COMMAND_NAME))) {
            handleCallback = ConstructCallback.CHANGE_PHOTO_QUANTITY_CALLBACK;
            Map<String, String> keys = MessageKeysParser.parseMessageKeysBody(callbackQuery.getMessage().getText());
            photosQuantity = Integer.parseInt(keys.get("photos_quantity"));
            audiosQuantity = Integer.parseInt(keys.get("audios_quantity"));
        }

        if (data.contains(ConstructCallback.SEND_CALLBACK.toCallbackString(COMMAND_NAME))) {
            handleCallback = ConstructCallback.SEND_CALLBACK;
        }

        int groupId = 0;
        if (data.contains(COMMAND_NAME + "_group_id")) {
            handleCallback = ConstructCallback.GROUP_CALLBACK;
            groupId = Integer.parseInt(data.replace(COMMAND_NAME + "_group_id", ""));
        }

        switch (handleCallback) {
            case CONSTRUCT_CALLBACK:
                sendContentSet(sender, callbackQuery, photosQuantity, audiosQuantity);
                break;
            case CHANGE_PHOTO_QUANTITY_CALLBACK:
                sendPhotoQuantityNumpad(sender, callbackQuery, audiosQuantity);
                break;
            case GROUP_CALLBACK:
                sendSetToGroup(sender, callbackQuery, groupId);
                break;
            case SEND_CALLBACK:
                sendGroupKeyboard(sender, callbackQuery);
                break;
            default:
                sendAudioQuantityNumpad(sender, callbackQuery, photosQuantity);
        }
    }

    private void sendContentSet(AbsSender sender, CallbackQuery callbackQuery, int photosQuantity, int audiosQuantity) {
        EditMessageText response = new EditMessageText();
        response.setChatId(callbackQuery.getMessage().getChatId());
        response.setMessageId(callbackQuery.getMessage().getMessageId());
        response.setParseMode(ParseMode.HTML);

        List<VkAttachment> attachments = new ArrayList<>();

        if (photosQuantity > 0) {
            VkContentStrategyService photoService = new VkContentStrategyService(new VkRandomPhotoContent());
            attachments.addAll(photoService.find(photosQuantity));
        }

        if (audiosQuantity > 0) {
            VkContentStrategyService audioService = new VkContentStrategyService(new VkRandomAudioContent());
            attachments.addAll(audioService.find(audiosQuantity));
        }

        response.setText(fillTextBody(attachments, photosQuantity, audiosQuantity));
        if (audiosQuantity > 0 || photosQuantity > 0) {
            response.setReplyMarkup(buildConstructKeyboard());
        }
        ResponseMessageDispatcher.send(sender, response);
    }

    private String fillTextBody(List<VkAttachment> attachments, int photosQuantity, int audiosQuantity) {
        StringBuilder text = new StringBuilder();
        if (audiosQuantity == 0 && photosQuantity == 0) {
            text.append("oopsy doopsy, nothing to look for...");
            return text.toString();
        } else {
            text.append("photos_quantity: ").append(photosQuantity).append("\n");
            text.append("audios_quantity: ").append(audiosQuantity).append("\n");
            text.append("\n");
        }

        if (photosQuantity > 0) {
            List<VkAttachment> photos = attachments.stream()
                    .filter(attach -> attach instanceof VkCustomPhoto)
                    .collect(Collectors.toList());

            int count = 1;
            for (VkAttachment photo : photos) {
                VkCustomPhoto vkCustomPhoto = (VkCustomPhoto) photo;
                URL photoURL = vkCustomPhoto.getLargestSize().getUrl();
                String vkAttachmentFormat = photo.toAttachmentString();

                text.append("photo_").append(count).append(": ");
                text.append("<a href=\"").append(photoURL).append("\">").append(vkAttachmentFormat).append("</a>");
                text.append("\n");
                count++;
            }
        }

        if (photosQuantity > 0 && audiosQuantity > 0) {
            text.append("\n");
        }

        if (audiosQuantity > 0) {
            List<VkAttachment> audios = attachments.stream()
                    .filter(attach -> attach instanceof VkCustomAudio)
                    .collect(Collectors.toList());

            int count = 1;
            for (VkAttachment audio : audios) {
                VkCustomAudio vkCustomAudio = (VkCustomAudio) audio;
                String prettyNameOfAudio = vkCustomAudio.toPrettyString();
                String vkAttachmentFormat = audio.toAttachmentString();

                text.append("audio_").append(count).append(": ");
                text.append(vkAttachmentFormat).append(" (").append(prettyNameOfAudio).append(")");
                text.append("\n");
                count++;
            }
        }

        return text.toString();
    }

    private void sendAudioQuantityNumpad(AbsSender sender, CallbackQuery callbackQuery, int photoQuantity) {
        EditMessageText response = new EditMessageText();
        response.setChatId(callbackQuery.getMessage().getChatId());
        response.setMessageId(callbackQuery.getMessage().getMessageId());
        response.setText("photos_quantity: " + photoQuantity + "\n\nChoose audios quantity: ");
        NumpadKeyboardBuilder numpad = new NumpadKeyboardBuilder(4, MAX_VK_ATTACHMENTS - photoQuantity);
        response.setReplyMarkup(numpad.build(COMMAND_NAME + "audio", true));
        ResponseMessageDispatcher.send(sender, response);
    }

    private void sendPhotoQuantityNumpad(AbsSender sender, CallbackQuery callbackQuery, int audioQuantity) {
        EditMessageText response = new EditMessageText();
        response.setChatId(callbackQuery.getMessage().getChatId());
        response.setMessageId(callbackQuery.getMessage().getMessageId());
        response.setText("audios_quantity: " + audioQuantity + "\n\nChoose photos quantity: ");
        NumpadKeyboardBuilder numpad = new NumpadKeyboardBuilder(4, MAX_VK_ATTACHMENTS - audioQuantity);
        response.setReplyMarkup(numpad.build(COMMAND_NAME + "photo", true));
        ResponseMessageDispatcher.send(sender, response);
    }

    private void sendSetToGroup(AbsSender sender, CallbackQuery callbackQuery, int groupId) {
        Map<String, String> keys = MessageKeysParser.parseMessageKeysBody(callbackQuery.getMessage().getText());
        List<String> audiosKeys = keys.keySet().stream().filter(key -> key.startsWith("audio_")).collect(Collectors.toList());
        List<String> photosKeys = keys.keySet().stream().filter(key -> key.startsWith("photo_")).collect(Collectors.toList());
        List<String> vkAttachments = new ArrayList<>();

        for (String key : audiosKeys) {
            vkAttachments.add(keys.get(key));
        }

        for (String key : photosKeys) {
            vkAttachments.add(keys.get(key));
        }

        VkCustomGroup vkGroup = VkGroupPool.getConcreteGroups(VkGroupObjective.HOST).stream()
                .filter(group -> group.getId() == groupId)
                .findFirst()
                .orElse(VkCustomGroup.EMPTY_INSTANCE);

        EditMessageReplyMarkup response = new EditMessageReplyMarkup();
        response.setChatId(callbackQuery.getMessage().getChatId());
        response.setMessageId(callbackQuery.getMessage().getMessageId());
        VkWallPostService service = new VkWallPostService();
        InlineKeyboardBuilder keyboardBuilder = new InlineKeyboardBuilder();
        boolean isRequestSuccessful = service.makePost(vkGroup, vkAttachments);
        if (isRequestSuccessful) {
            keyboardBuilder.addButton(new InlineKeyboardButton()
                    .setText("Post was send to " + vkGroup.getName() + " group!")
                    .setUrl(vkGroup.getUrl()));
        } else {
            keyboardBuilder.addButton(new InlineKeyboardButton()
                    .setText("Oops... something was broken on the way!"));
        }
        response.setReplyMarkup(keyboardBuilder.build());
        ResponseMessageDispatcher.send(sender, response);
    }

    private void sendGroupKeyboard(AbsSender sender, CallbackQuery callbackQuery) {
        EditMessageReplyMarkup response = new EditMessageReplyMarkup();
        response.setChatId(callbackQuery.getMessage().getChatId());
        response.setMessageId(callbackQuery.getMessage().getMessageId());
        response.setReplyMarkup(new HostGroupKeyboard().build(COMMAND_NAME, true));
        ResponseMessageDispatcher.send(sender, response);
    }

    private InlineKeyboardMarkup buildConstructKeyboard() {
        return new InlineKeyboardBuilder()
                .addButton(new InlineKeyboardButton()
                    .setText("Change content set")
                    .setCallbackData(ConstructCallback.CHANGE_SET_CALLBACK.toCallbackString(COMMAND_NAME)))
                .nextLine()
                .addButton(new InlineKeyboardButton()
                        .setText("Change audio quantity")
                        .setCallbackData(ConstructCallback.CHANGE_AUDIO_QUANTITY_CALLBACK.toCallbackString(COMMAND_NAME)))
                .addButton(new InlineKeyboardButton()
                        .setText("Change photo quantity")
                        .setCallbackData(ConstructCallback.CHANGE_PHOTO_QUANTITY_CALLBACK.toCallbackString(COMMAND_NAME)))
                .nextLine()
                .addButton(new InlineKeyboardButton()
                        .setText("Send")
                        .setCallbackData(ConstructCallback.SEND_CALLBACK.toCallbackString(COMMAND_NAME)))
                .nextLine()
                .addButton(new InlineKeyboardButton()
                        .setText("Cancel")
                        .setCallbackData(GlobalCallback.DELETE_MESSAGE.name()))
                .build();
    }
}
